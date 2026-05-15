package io.soqlexplorer.salesforce;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the Salesforce OAuth Connected App and HTTP client.
 *
 * <p>All fields except {@code redirectUri} default to safe placeholders so the application boots
 * locally without a Connected App configured; OAuth calls will fail with a clear authentication
 * error if {@code clientId} / {@code clientSecret} are not set.
 */
@ConfigurationProperties(prefix = "soqlexplorer.salesforce")
public class SalesforceProperties {

  /** OAuth Connected App consumer key. */
  private String clientId = "";

  /** OAuth Connected App consumer secret. */
  private String clientSecret = "";

  /** Absolute URL the SF authorization server will redirect to after the user consents. */
  private String redirectUri = "http://localhost:8080/connections/oauth/callback";

  /** Comma-separated scopes requested during authorization. */
  private String scopes = "api refresh_token offline_access";

  /** Salesforce REST API version used for /services/data calls (e.g. v60.0). */
  private String apiVersion = "v60.0";

  /** Cushion subtracted from access-token expiry when deciding whether to refresh. */
  private Duration tokenRefreshSkew = Duration.ofMinutes(2);

  /** HTTP connect + read timeout for the Salesforce REST client. */
  private Duration httpTimeout = Duration.ofSeconds(30);

  /** TTL clamp for the in-memory access token cache. */
  private Duration accessTokenCacheTtl = Duration.ofMinutes(110);

  /** Base64-encoded 256-bit key used for AES-GCM encryption of refresh tokens at rest. */
  private String refreshTokenEncryptionKey = "";

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public String getScopes() {
    return scopes;
  }

  public void setScopes(String scopes) {
    this.scopes = scopes;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public Duration getTokenRefreshSkew() {
    return tokenRefreshSkew;
  }

  public void setTokenRefreshSkew(Duration tokenRefreshSkew) {
    this.tokenRefreshSkew = tokenRefreshSkew;
  }

  public Duration getHttpTimeout() {
    return httpTimeout;
  }

  public void setHttpTimeout(Duration httpTimeout) {
    this.httpTimeout = httpTimeout;
  }

  public Duration getAccessTokenCacheTtl() {
    return accessTokenCacheTtl;
  }

  public void setAccessTokenCacheTtl(Duration accessTokenCacheTtl) {
    this.accessTokenCacheTtl = accessTokenCacheTtl;
  }

  public String getRefreshTokenEncryptionKey() {
    return refreshTokenEncryptionKey;
  }

  public void setRefreshTokenEncryptionKey(String refreshTokenEncryptionKey) {
    this.refreshTokenEncryptionKey = refreshTokenEncryptionKey;
  }
}
