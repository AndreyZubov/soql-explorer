package io.soqlexplorer.salesforce.oauth;

import io.soqlexplorer.application.ports.clock.ClockPort;
import io.soqlexplorer.application.ports.salesforce.OAuthAuthorizationRequest;
import io.soqlexplorer.application.ports.salesforce.OAuthExchangeResult;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayException;
import io.soqlexplorer.application.ports.salesforce.SalesforceOAuthPort;
import io.soqlexplorer.domain.connection.Environment;
import io.soqlexplorer.domain.user.UserId;
import io.soqlexplorer.salesforce.SalesforceProperties;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing {@link SalesforceOAuthPort} via the Web Server Flow (RFC 6749 §4.1) plus
 * PKCE (RFC 7636).
 */
@Component
public class SalesforceOAuthAdapter implements SalesforceOAuthPort {

  /** State token lifetime — generous because users may stop to log in to Salesforce. */
  private static final Duration STATE_TTL = Duration.ofMinutes(15);

  private final OAuthStateSigner signer;
  private final SalesforceTokenEndpoint tokenEndpoint;
  private final SalesforceProperties props;
  private final ClockPort clock;

  public SalesforceOAuthAdapter(
      OAuthStateSigner signer,
      SalesforceTokenEndpoint tokenEndpoint,
      SalesforceProperties props,
      ClockPort clock) {
    this.signer = signer;
    this.tokenEndpoint = tokenEndpoint;
    this.props = props;
    this.clock = clock;
  }

  @Override
  public OAuthAuthorizationRequest startAuthorization(UserId ownerId, Environment environment) {
    if (props.getClientId() == null || props.getClientId().isBlank()) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH,
          "Salesforce client_id is not configured (set soqlexplorer.salesforce.client-id)");
    }
    String verifier = Pkce.generateVerifier();
    String challenge = Pkce.challenge(verifier);
    Instant expiresAt = clock.now().plus(STATE_TTL);
    String state = signer.sign(OAuthState.newFor(ownerId, environment, verifier, expiresAt));

    String url =
        environment.loginHost()
            + "/services/oauth2/authorize"
            + "?response_type=code"
            + "&client_id="
            + encode(props.getClientId())
            + "&redirect_uri="
            + encode(props.getRedirectUri())
            + "&scope="
            + encode(props.getScopes())
            + "&code_challenge="
            + encode(challenge)
            + "&code_challenge_method=S256"
            + "&state="
            + encode(state);

    return new OAuthAuthorizationRequest(URI.create(url), state, environment);
  }

  @Override
  public OAuthExchangeResult exchangeAuthorizationCode(String code, String state) {
    OAuthState payload = signer.verify(state);
    if (payload.expiresAtEpochSecond() < clock.now().getEpochSecond()) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH, "OAuth state has expired");
    }
    Environment env = payload.env();
    SalesforceTokenEndpoint.TokenResponse response =
        tokenEndpoint.exchangeAuthorizationCode(env.loginHost(), code, payload.pkceVerifier());
    if (response.refreshToken() == null
        || response.instanceUrl() == null
        || response.orgId() == null) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH,
          "Token endpoint did not return refresh_token / instance_url / id");
    }
    return new OAuthExchangeResult(
        UserId.of(payload.userId()),
        env,
        response.accessToken(),
        response.refreshToken(),
        response.instanceUrl(),
        response.orgId(),
        response.issuedAt().plus(Duration.ofMinutes(110)));
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
