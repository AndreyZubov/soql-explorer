package io.soqlexplorer.salesforce.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** Utilities for the OAuth 2.0 PKCE extension (RFC 7636). */
final class Pkce {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

  private Pkce() {}

  /** Generates a 43-character URL-safe code verifier (32 bytes of entropy, RFC 7636 §4.1). */
  static String generateVerifier() {
    byte[] random = new byte[32];
    RANDOM.nextBytes(random);
    return URL_ENCODER.encodeToString(random);
  }

  /** SHA-256 code challenge derived from the verifier, base64url-encoded without padding. */
  static String challenge(String verifier) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
      return URL_ENCODER.encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
