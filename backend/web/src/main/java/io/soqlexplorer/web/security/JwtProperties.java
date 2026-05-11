package io.soqlexplorer.web.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe configuration binding for JWT issuance.
 *
 * <p>The signing key must be supplied as a base64 string with at least 256 bits of entropy. In
 * Step 5 this will come from Vault; in local/test profiles it's a dev value loaded from {@code
 * .env.example}.
 */
@Validated
@ConfigurationProperties(prefix = "soqlexplorer.security.jwt")
public class JwtProperties {

  @NotBlank private String issuer = "soql-explorer";

  /** Base64-encoded HS256 signing key. Must decode to at least 32 bytes. */
  @NotBlank private String secret;

  @Min(60)
  private long accessTokenTtlSeconds = 900;

  @Min(60)
  private long refreshTokenTtlSeconds = 60L * 60 * 24 * 7;

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getAccessTokenTtlSeconds() {
    return accessTokenTtlSeconds;
  }

  public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
    this.accessTokenTtlSeconds = accessTokenTtlSeconds;
  }

  public long getRefreshTokenTtlSeconds() {
    return refreshTokenTtlSeconds;
  }

  public void setRefreshTokenTtlSeconds(long refreshTokenTtlSeconds) {
    this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
  }
}
