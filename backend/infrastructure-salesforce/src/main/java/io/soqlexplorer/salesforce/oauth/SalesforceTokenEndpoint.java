package io.soqlexplorer.salesforce.oauth;

import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayException;
import io.soqlexplorer.salesforce.SalesforceProperties;
import io.soqlexplorer.salesforce.SalesforceWebClientFactory;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Thin client over the Salesforce {@code /services/oauth2/token} endpoint.
 *
 * <p>Encapsulates both the authorization-code exchange (initial connection) and the
 * refresh-token grant (token expiry). The two calls share a near-identical request shape — only
 * the {@code grant_type} differs — so they live together.
 */
@Component
public class SalesforceTokenEndpoint {

  private static final String TOKEN_PATH = "/services/oauth2/token";

  private final SalesforceWebClientFactory factory;
  private final SalesforceProperties props;

  public SalesforceTokenEndpoint(SalesforceWebClientFactory factory, SalesforceProperties props) {
    this.factory = factory;
    this.props = props;
  }

  public TokenResponse exchangeAuthorizationCode(String loginHost, String code, String verifier) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "authorization_code");
    form.add("client_id", props.getClientId());
    form.add("client_secret", props.getClientSecret());
    form.add("redirect_uri", props.getRedirectUri());
    form.add("code", code);
    form.add("code_verifier", verifier);
    return call(loginHost, form);
  }

  public TokenResponse refresh(String loginHost, String refreshToken) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "refresh_token");
    form.add("client_id", props.getClientId());
    form.add("client_secret", props.getClientSecret());
    form.add("refresh_token", refreshToken);
    return call(loginHost, form);
  }

  @SuppressWarnings("unchecked")
  private TokenResponse call(String baseHost, MultiValueMap<String, String> form) {
    try {
      Map<String, Object> response =
          factory
              .base(baseHost)
              .post()
              .uri(TOKEN_PATH)
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .body(BodyInserters.fromFormData(form))
              .retrieve()
              .bodyToMono(Map.class)
              .block();
      if (response == null) {
        throw new SalesforceGatewayException(
            SalesforceGatewayException.Kind.AUTH, "Empty token response");
      }
      Object accessToken = response.get("access_token");
      if (accessToken == null) {
        throw new SalesforceGatewayException(
            SalesforceGatewayException.Kind.AUTH,
            "Token response missing access_token: " + response.get("error"));
      }
      String instanceUrl = (String) response.get("instance_url");
      String id = (String) response.get("id");
      String refreshToken = (String) response.get("refresh_token");
      Instant issuedAt =
          response.get("issued_at") instanceof String s
              ? Instant.ofEpochMilli(Long.parseLong(s))
              : Instant.now();
      return new TokenResponse(
          (String) accessToken,
          refreshToken,
          instanceUrl != null ? URI.create(instanceUrl) : null,
          extractOrgId(id),
          issuedAt);
    } catch (WebClientResponseException e) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH,
          "Token endpoint rejected the request: " + e.getStatusCode(),
          e);
    }
  }

  /** {@code id} is of the form {@code https://login.salesforce.com/id/<orgId>/<userId>}. */
  private static String extractOrgId(String id) {
    if (id == null) {
      return null;
    }
    String[] parts = id.split("/");
    return parts.length >= 2 ? parts[parts.length - 2] : null;
  }

  /** Subset of the token endpoint response the application cares about. */
  public record TokenResponse(
      String accessToken,
      String refreshToken,
      URI instanceUrl,
      String orgId,
      Instant issuedAt) {}
}
