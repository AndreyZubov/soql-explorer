package io.soqlexplorer.salesforce;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.soqlexplorer.application.ports.salesforce.AccessTokenPort;
import io.soqlexplorer.application.ports.salesforce.QueryPage;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayException;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayPort;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.query.SoqlQuery;
import io.soqlexplorer.salesforce.token.TokenRefresher;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Reactive WebClient-backed implementation of {@link SalesforceGatewayPort} (and
 * {@link TokenRefresher}).
 *
 * <p>Each call:
 *
 * <ol>
 *   <li>Acquires a bearer access token from {@link AccessTokenPort} (which refreshes if needed).
 *   <li>Sends the request, capturing the {@code Sforce-Limit-Info} header.
 *   <li>On 401 invalidates the cached access token and retries exactly once.
 *   <li>On 429 / 5xx surfaces a typed {@link SalesforceGatewayException}; Resilience4j retries
 *       the {@link SalesforceGatewayException.Kind#UPSTREAM_5XX} kind a few times before opening
 *       the circuit.
 * </ol>
 */
@Component
public class SalesforceGatewayAdapter implements SalesforceGatewayPort, TokenRefresher {

  private static final String DATA_PATH = "/services/data/";
  private static final ParameterizedTypeReference<Map<String, Object>> MAP_REF =
      new ParameterizedTypeReference<>() {};

  private final SalesforceWebClientFactory factory;
  private final AccessTokenPort accessTokens;
  private final ApiUsageTracker usage;
  private final SalesforceProperties props;
  private final io.soqlexplorer.salesforce.oauth.SalesforceTokenEndpoint tokenEndpoint;

  public SalesforceGatewayAdapter(
      SalesforceWebClientFactory factory,
      AccessTokenPort accessTokens,
      ApiUsageTracker usage,
      SalesforceProperties props,
      io.soqlexplorer.salesforce.oauth.SalesforceTokenEndpoint tokenEndpoint) {
    this.factory = factory;
    this.accessTokens = accessTokens;
    this.usage = usage;
    this.props = props;
    this.tokenEndpoint = tokenEndpoint;
  }

  // -------------------------------------------------------------------------
  // TokenRefresher
  // -------------------------------------------------------------------------

  @Override
  public Refreshed refresh(SalesforceConnection connection, String refreshToken) {
    var response = tokenEndpoint.refresh(connection.environment().loginHost(), refreshToken);
    Instant issuedAt = response.issuedAt() != null ? response.issuedAt() : Instant.now();
    return new Refreshed(response.accessToken(), issuedAt.plus(java.time.Duration.ofMinutes(110)));
  }

  // -------------------------------------------------------------------------
  // SalesforceGatewayPort
  // -------------------------------------------------------------------------

  @Override
  @CircuitBreaker(name = "salesforce")
  @Retry(name = "salesforce")
  public List<String> listSObjects(SalesforceConnection connection) {
    String path = DATA_PATH + props.getApiVersion() + "/sobjects";
    Map<String, Object> response = call(connection, path, MAP_REF, MAP_REF);
    Object raw = response.get("sobjects");
    if (!(raw instanceof List<?> list)) {
      return List.of();
    }
    return list.stream()
        .filter(Map.class::isInstance)
        .map(item -> ((Map<?, ?>) item).get("name"))
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .toList();
  }

  @Override
  @CircuitBreaker(name = "salesforce")
  @Retry(name = "salesforce")
  public Map<String, Object> describeSObject(SalesforceConnection connection, String sObjectName) {
    String path = DATA_PATH + props.getApiVersion() + "/sobjects/" + encodeSegment(sObjectName) + "/describe";
    return call(connection, path, MAP_REF, MAP_REF);
  }

  @Override
  @CircuitBreaker(name = "salesforce")
  @Retry(name = "salesforce")
  public Map<String, Object> getRelationships(SalesforceConnection connection, String sObjectName) {
    Map<String, Object> describe = describeSObject(connection, sObjectName);
    Map<String, Object> projected = new LinkedHashMap<>();
    projected.put("name", describe.get("name"));
    projected.put("childRelationships", describe.getOrDefault("childRelationships", List.of()));
    Object fields = describe.get("fields");
    if (fields instanceof List<?> fieldList) {
      projected.put(
          "referenceFields",
          fieldList.stream()
              .filter(Map.class::isInstance)
              .map(f -> (Map<?, ?>) f)
              .filter(f -> "reference".equals(f.get("type")))
              .map(
                  f -> {
                    Map<String, Object> projection = new LinkedHashMap<>();
                    projection.put("name", String.valueOf(f.get("name")));
                    projection.put("relationshipName", String.valueOf(f.get("relationshipName")));
                    Object refs = f.get("referenceTo");
                    projection.put("referenceTo", refs != null ? refs : List.of());
                    return projection;
                  })
              .toList());
    } else {
      projected.put("referenceFields", List.of());
    }
    return projected;
  }

  @Override
  @CircuitBreaker(name = "salesforce")
  @Retry(name = "salesforce")
  public QueryPage executeQuery(SalesforceConnection connection, SoqlQuery query) {
    String path =
        DATA_PATH
            + props.getApiVersion()
            + "/query?q="
            + URLEncoder.encode(query.text(), StandardCharsets.UTF_8);
    return toQueryPage(call(connection, path, MAP_REF, MAP_REF));
  }

  @Override
  @CircuitBreaker(name = "salesforce")
  @Retry(name = "salesforce")
  public QueryPage fetchNextPage(SalesforceConnection connection, String nextRecordsUrl) {
    if (nextRecordsUrl == null || nextRecordsUrl.isBlank()) {
      throw new IllegalArgumentException("nextRecordsUrl must not be blank");
    }
    return toQueryPage(call(connection, nextRecordsUrl, MAP_REF, MAP_REF));
  }

  // -------------------------------------------------------------------------
  // Low-level HTTP plumbing
  // -------------------------------------------------------------------------

  private <T> T call(
      SalesforceConnection connection,
      String pathOrAbsolute,
      ParameterizedTypeReference<T> bodyRef,
      ParameterizedTypeReference<T> retriedBodyRef) {
    return doCall(connection, pathOrAbsolute, bodyRef, false);
  }

  private <T> T doCall(
      SalesforceConnection connection,
      String pathOrAbsolute,
      ParameterizedTypeReference<T> bodyRef,
      boolean alreadyRetriedAuth) {
    String token = accessTokens.getAccessToken(connection);
    WebClient client = factory.base(connection.instanceUrl().toString());
    URI target = URI.create(pathOrAbsolute);

    try {
      WebClient.ResponseSpec spec =
          client
              .get()
              .uri(target.toString())
              .headers(h -> h.setBearerAuth(token))
              .retrieve();

      // Use exchangeToMono-style toEntity so we can inspect the Sforce-Limit-Info header.
      var entity = spec.toEntity(bodyRef).block();
      if (entity == null) {
        throw new SalesforceGatewayException(
            SalesforceGatewayException.Kind.UPSTREAM_5XX, "Empty response from Salesforce");
      }
      HttpHeaders headers = entity.getHeaders();
      usage.record(connection.id(), headers.getFirst("Sforce-Limit-Info"));
      return entity.getBody();
    } catch (WebClientResponseException e) {
      HttpStatusCode status = e.getStatusCode();
      if (status.value() == 401 && !alreadyRetriedAuth) {
        accessTokens.invalidate(connection);
        return doCall(connection, pathOrAbsolute, bodyRef, true);
      }
      throw mapHttpStatus(status, e);
    } catch (CallNotPermittedException e) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.CIRCUIT_OPEN, "Salesforce circuit breaker is open", e);
    } catch (RuntimeException e) {
      if (e.getCause() instanceof TimeoutException) {
        throw new SalesforceGatewayException(
            SalesforceGatewayException.Kind.TIMEOUT, "Salesforce call timed out", e);
      }
      throw e;
    }
  }

  private static SalesforceGatewayException mapHttpStatus(
      HttpStatusCode status, WebClientResponseException cause) {
    if (status.value() == 401 || status.value() == 403) {
      return new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH, "Salesforce rejected the credentials", cause);
    }
    if (status.value() == 429) {
      return new SalesforceGatewayException(
          SalesforceGatewayException.Kind.RATE_LIMITED, "Salesforce rate limit hit", cause);
    }
    if (status.is4xxClientError()) {
      return new SalesforceGatewayException(
          SalesforceGatewayException.Kind.BAD_REQUEST,
          "Salesforce rejected the request: " + status + " — " + cause.getResponseBodyAsString(),
          cause);
    }
    if (status.is5xxServerError()) {
      return new SalesforceGatewayException(
          SalesforceGatewayException.Kind.UPSTREAM_5XX,
          "Salesforce upstream failure: " + status,
          cause);
    }
    return new SalesforceGatewayException(
        SalesforceGatewayException.Kind.UPSTREAM_5XX, "Unexpected Salesforce status: " + status, cause);
  }

  @SuppressWarnings("unchecked")
  private static QueryPage toQueryPage(Map<String, Object> body) {
    int totalSize =
        body.get("totalSize") instanceof Number n ? n.intValue() : 0;
    boolean done = Boolean.TRUE.equals(body.get("done"));
    Optional<String> next = Optional.ofNullable((String) body.get("nextRecordsUrl"));
    List<Map<String, Object>> records;
    if (body.get("records") instanceof List<?> raw) {
      records =
          raw.stream()
              .filter(Map.class::isInstance)
              .map(item -> (Map<String, Object>) item)
              .toList();
    } else {
      records = List.of();
    }
    return new QueryPage(records, totalSize, done, next);
  }

  private static String encodeSegment(String segment) {
    return URLEncoder.encode(segment, StandardCharsets.UTF_8);
  }
}
