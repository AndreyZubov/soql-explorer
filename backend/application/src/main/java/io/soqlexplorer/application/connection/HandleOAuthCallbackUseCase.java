package io.soqlexplorer.application.connection;

import io.soqlexplorer.domain.connection.SalesforceConnection;

/**
 * Inbound use case for completing an OAuth Web Server Flow callback.
 *
 * <p>Exchanges the authorization code for tokens, encrypts the refresh token, and persists a new
 * {@link SalesforceConnection}. Returns the persisted aggregate so the caller can build a
 * redirect URL pointing to the connection list (or the explorer for the new connection).
 *
 * <p>The signed {@code state} carries the originating user and the chosen environment — the
 * controller does not need to identify the caller separately.
 */
public interface HandleOAuthCallbackUseCase {

  SalesforceConnection complete(String code, String state);
}
