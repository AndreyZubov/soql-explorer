package io.soqlexplorer.salesforce.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.soqlexplorer.application.ports.salesforce.AccessTokenPort;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayException;
import io.soqlexplorer.application.ports.security.TokenCipherPort;
import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.salesforce.SalesforceProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * In-memory implementation of {@link AccessTokenPort}.
 *
 * <p>Caches one access token per connection until it is close to expiring (see {@code
 * tokenRefreshSkew}). Cache evictions and explicit invalidations both cause the next call to
 * refresh.
 */
@Component
public class AccessTokenCache implements AccessTokenPort {

  private final Cache<ConnectionId, CachedToken> cache;
  private final TokenRefresher refresher;
  private final TokenCipherPort cipher;
  private final SalesforceProperties props;

  public AccessTokenCache(TokenRefresher refresher, TokenCipherPort cipher, SalesforceProperties props) {
    this.refresher = Objects.requireNonNull(refresher, "refresher");
    this.cipher = Objects.requireNonNull(cipher, "cipher");
    this.props = Objects.requireNonNull(props, "props");
    this.cache =
        Caffeine.newBuilder()
            .expireAfterWrite(props.getAccessTokenCacheTtl())
            .maximumSize(10_000)
            .build();
  }

  @Override
  public String getAccessToken(SalesforceConnection connection) {
    CachedToken cached = cache.getIfPresent(connection.id());
    Instant now = Instant.now();
    if (cached != null && cached.expiresAt().isAfter(now.plus(props.getTokenRefreshSkew()))) {
      return cached.token();
    }
    return refresh(connection, now);
  }

  @Override
  public void invalidate(SalesforceConnection connection) {
    cache.invalidate(connection.id());
  }

  private synchronized String refresh(SalesforceConnection connection, Instant now) {
    // Double-checked: another thread might have refreshed while we waited on the monitor.
    CachedToken existing = cache.getIfPresent(connection.id());
    if (existing != null && existing.expiresAt().isAfter(now.plus(props.getTokenRefreshSkew()))) {
      return existing.token();
    }
    String refreshTokenPlain;
    try {
      refreshTokenPlain = cipher.decrypt(connection.refreshToken());
    } catch (RuntimeException e) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH, "Refresh token decryption failed", e);
    }
    TokenRefresher.Refreshed refreshed = refresher.refresh(connection, refreshTokenPlain);
    Instant expiresAt = refreshed.expiresAt() != null
        ? refreshed.expiresAt()
        : now.plus(Duration.ofMinutes(110));
    cache.put(connection.id(), new CachedToken(refreshed.accessToken(), expiresAt));
    return refreshed.accessToken();
  }

  private record CachedToken(String token, Instant expiresAt) {}
}
