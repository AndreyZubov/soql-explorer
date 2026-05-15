package io.soqlexplorer.application.ports.salesforce;

import io.soqlexplorer.domain.connection.Environment;
import io.soqlexplorer.domain.user.UserId;
import java.net.URI;
import java.util.Objects;

/**
 * Outcome of exchanging an OAuth authorization code for tokens.
 *
 * <p>The refresh token arrives in clear text from Salesforce and is encrypted before it leaves
 * the calling use case via the {@code TokenCipherPort}. The verified {@link UserId} and
 * {@link Environment} are read from the signed {@code state} so the callback handler can be a
 * thin shim that only forwards the {@code code} / {@code state} pair.
 */
public record OAuthExchangeResult(
    UserId ownerId,
    Environment environment,
    String accessToken,
    String refreshToken,
    URI instanceUrl,
    String orgId,
    java.time.Instant accessTokenExpiresAt) {

  public OAuthExchangeResult {
    Objects.requireNonNull(ownerId, "ownerId");
    Objects.requireNonNull(environment, "environment");
    Objects.requireNonNull(accessToken, "accessToken");
    Objects.requireNonNull(refreshToken, "refreshToken");
    Objects.requireNonNull(instanceUrl, "instanceUrl");
    Objects.requireNonNull(orgId, "orgId");
    Objects.requireNonNull(accessTokenExpiresAt, "accessTokenExpiresAt");
  }
}
