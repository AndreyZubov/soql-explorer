package io.soqlexplorer.application.ports.salesforce;

import io.soqlexplorer.domain.connection.Environment;
import io.soqlexplorer.domain.user.UserId;

/**
 * Outbound port for the OAuth 2.0 Web Server Flow against Salesforce.
 *
 * <p>The flow is split into two methods so the application layer can model it as two distinct
 * use cases (start + handle callback) and the controllers can wire them to two endpoints.
 *
 * <p>State is opaque to the application layer: the adapter produces a signed token that binds the
 * requesting user, the environment, and the PKCE verifier; the adapter verifies it on callback.
 */
public interface SalesforceOAuthPort {

  /**
   * Begins an authorization flow, returning the URL to redirect the browser to and the opaque
   * state nonce that the callback handler must echo back.
   */
  OAuthAuthorizationRequest startAuthorization(UserId ownerId, Environment environment);

  /**
   * Verifies the state, exchanges the authorization code for tokens, and returns them along with
   * the verified ownerId / environment captured by the original {@link #startAuthorization} call.
   *
   * @throws SalesforceGatewayException with {@link SalesforceGatewayException.Kind#AUTH} when
   *     the state is invalid, the code is rejected, or PKCE verification fails.
   */
  OAuthExchangeResult exchangeAuthorizationCode(String code, String state);
}
