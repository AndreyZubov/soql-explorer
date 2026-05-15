package io.soqlexplorer.salesforce.oauth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.soqlexplorer.domain.connection.Environment;
import io.soqlexplorer.domain.user.UserId;
import java.time.Instant;
import java.util.UUID;

/**
 * Payload embedded in the OAuth {@code state} parameter.
 *
 * <p>The fields bind the callback to a specific user + environment + PKCE verifier and add an
 * expiry so a stolen state cannot be replayed indefinitely. The {@code nonce} guarantees freshness
 * even if the same user starts two flows in parallel.
 */
record OAuthState(
    @JsonProperty("u") UUID userId,
    @JsonProperty("e") String environment,
    @JsonProperty("v") String pkceVerifier,
    @JsonProperty("n") String nonce,
    @JsonProperty("exp") long expiresAtEpochSecond) {

  @JsonCreator
  OAuthState {}

  static OAuthState newFor(UserId userId, Environment env, String verifier, Instant expiresAt) {
    return new OAuthState(
        userId.value(),
        env.name(),
        verifier,
        UUID.randomUUID().toString(),
        expiresAt.getEpochSecond());
  }

  Environment env() {
    return Environment.valueOf(environment);
  }
}
