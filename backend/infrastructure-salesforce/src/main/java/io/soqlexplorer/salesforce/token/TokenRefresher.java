package io.soqlexplorer.salesforce.token;

import io.soqlexplorer.domain.connection.SalesforceConnection;
import java.time.Instant;

/**
 * Indirection that performs an OAuth {@code grant_type=refresh_token} round-trip.
 *
 * <p>Lives in its own interface so {@link AccessTokenCache} can be unit-tested without spinning
 * up a WebClient mock.
 */
public interface TokenRefresher {

  Refreshed refresh(SalesforceConnection connection, String refreshToken);

  /** Result of a refresh — Salesforce returns {@code access_token} and an issued-at timestamp. */
  record Refreshed(String accessToken, Instant expiresAt) {}
}
