package io.soqlexplorer.application.ports.salesforce;

import io.soqlexplorer.domain.connection.SalesforceConnection;

/**
 * Outbound port that returns a usable Salesforce access token for a connection, refreshing it via
 * the stored refresh token if the current one is missing or expired.
 *
 * <p>Implementations cache tokens in memory (see {@code AccessTokenCache} in
 * {@code infrastructure-salesforce}). The application layer never sees the raw secret string — it
 * passes the {@link SalesforceConnection} aggregate and trusts the adapter to keep tokens fresh.
 */
public interface AccessTokenPort {

  /**
   * Returns a non-expired access token for the supplied connection. If the cached token is missing
   * or near expiry, the adapter performs an OAuth refresh-token round-trip.
   *
   * @throws SalesforceGatewayException with {@link SalesforceGatewayException.Kind#AUTH} when the
   *     refresh token has been revoked.
   */
  String getAccessToken(SalesforceConnection connection);

  /** Drops any cached access token for the supplied connection. */
  void invalidate(SalesforceConnection connection);
}
