package io.soqlexplorer.salesforce.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayException;
import io.soqlexplorer.salesforce.SalesforceProperties;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Serializes and verifies the OAuth {@code state} parameter.
 *
 * <p>Format: {@code base64url(json).base64url(HMAC-SHA256)}.
 *
 * <p>The HMAC key is derived from the refresh-token encryption key with a domain separator so the
 * same byte material cannot be cross-used as a token cipher key.
 */
@Component
public class OAuthStateSigner {

  private static final byte[] DOMAIN_SEPARATOR = "oauth-state-v1".getBytes(StandardCharsets.UTF_8);
  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

  private final ObjectMapper mapper;
  private final byte[] hmacKey;

  public OAuthStateSigner(SalesforceProperties props, ObjectMapper mapper) {
    this.mapper = mapper;
    this.hmacKey = deriveHmacKey(props.getRefreshTokenEncryptionKey());
  }

  String sign(OAuthState state) {
    byte[] payload;
    try {
      payload = mapper.writeValueAsBytes(state);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to encode OAuth state", e);
    }
    byte[] signature = hmac(payload);
    return URL_ENCODER.encodeToString(payload) + "." + URL_ENCODER.encodeToString(signature);
  }

  OAuthState verify(String state) {
    if (state == null || state.isBlank()) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH, "OAuth state missing");
    }
    int dot = state.indexOf('.');
    if (dot <= 0 || dot == state.length() - 1) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH, "OAuth state malformed");
    }
    byte[] payload;
    byte[] signature;
    try {
      payload = URL_DECODER.decode(state.substring(0, dot));
      signature = URL_DECODER.decode(state.substring(dot + 1));
    } catch (IllegalArgumentException e) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH, "OAuth state encoding invalid", e);
    }
    byte[] expected = hmac(payload);
    if (!MessageDigest.isEqual(expected, signature)) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH, "OAuth state signature mismatch");
    }
    try {
      return mapper.readValue(payload, OAuthState.class);
    } catch (Exception e) {
      throw new SalesforceGatewayException(
          SalesforceGatewayException.Kind.AUTH, "OAuth state payload invalid", e);
    }
  }

  private byte[] hmac(byte[] payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
      return mac.doFinal(payload);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException("HMAC-SHA256 unavailable", e);
    }
  }

  private static byte[] deriveHmacKey(String configured) {
    if (configured == null || configured.isBlank()) {
      throw new IllegalStateException(
          "soqlexplorer.salesforce.refresh-token-encryption-key must be set to derive the OAuth state key");
    }
    byte[] keyMaterial;
    try {
      keyMaterial = Base64.getDecoder().decode(configured);
    } catch (IllegalArgumentException notBase64) {
      keyMaterial = configured.getBytes(StandardCharsets.UTF_8);
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(DOMAIN_SEPARATOR);
      digest.update(keyMaterial);
      return digest.digest();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }

}
