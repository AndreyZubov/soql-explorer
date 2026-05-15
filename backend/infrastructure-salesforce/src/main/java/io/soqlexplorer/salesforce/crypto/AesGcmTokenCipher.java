package io.soqlexplorer.salesforce.crypto;

import io.soqlexplorer.application.ports.security.TokenCipherPort;
import io.soqlexplorer.domain.connection.EncryptedToken;
import io.soqlexplorer.salesforce.SalesforceProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * AES-256-GCM implementation of {@link TokenCipherPort}.
 *
 * <p>Wire format of the {@link EncryptedToken} ciphertext:
 *
 * <pre>
 *   [ 1 byte version = 0x01 ][ 12 bytes IV ][ ciphertext + 16 byte tag ]
 * </pre>
 *
 * <p>The version byte gives us a non-breaking escape hatch if we ever rotate the algorithm.
 * The IV is generated per encryption from a {@link SecureRandom}; reusing an IV with the same
 * key in GCM is catastrophic.
 */
@Component
public class AesGcmTokenCipher implements TokenCipherPort {

  private static final byte VERSION = 0x01;
  private static final int IV_LENGTH_BYTES = 12;
  private static final int TAG_LENGTH_BITS = 128;
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";

  private final SecretKeySpec key;
  private final SecureRandom random = new SecureRandom();

  public AesGcmTokenCipher(SalesforceProperties props) {
    byte[] keyBytes = decodeKey(props.getRefreshTokenEncryptionKey());
    if (keyBytes.length != 32) {
      throw new IllegalStateException(
          "soqlexplorer.salesforce.refresh-token-encryption-key must decode to 32 bytes (AES-256)");
    }
    this.key = new SecretKeySpec(keyBytes, "AES");
  }

  @Override
  public EncryptedToken encrypt(String plaintext) {
    if (plaintext == null) {
      throw new IllegalArgumentException("plaintext must not be null");
    }
    byte[] iv = new byte[IV_LENGTH_BYTES];
    random.nextBytes(iv);
    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
      byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      ByteBuffer buf = ByteBuffer.allocate(1 + iv.length + ct.length);
      buf.put(VERSION).put(iv).put(ct);
      return new EncryptedToken(buf.array());
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to encrypt token", e);
    }
  }

  @Override
  public String decrypt(EncryptedToken token) {
    if (token == null) {
      throw new IllegalArgumentException("token must not be null");
    }
    byte[] payload = token.ciphertext();
    if (payload.length < 1 + IV_LENGTH_BYTES + 16) {
      throw new IllegalArgumentException("Encrypted payload is too short");
    }
    if (payload[0] != VERSION) {
      throw new IllegalStateException("Unsupported ciphertext version: " + payload[0]);
    }
    byte[] iv = new byte[IV_LENGTH_BYTES];
    System.arraycopy(payload, 1, iv, 0, IV_LENGTH_BYTES);
    byte[] ct = new byte[payload.length - 1 - IV_LENGTH_BYTES];
    System.arraycopy(payload, 1 + IV_LENGTH_BYTES, ct, 0, ct.length);
    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
      byte[] pt = cipher.doFinal(ct);
      return new String(pt, StandardCharsets.UTF_8);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to decrypt token", e);
    }
  }

  private static byte[] decodeKey(String configured) {
    if (configured == null || configured.isBlank()) {
      throw new IllegalStateException(
          "soqlexplorer.salesforce.refresh-token-encryption-key must be set"
              + " (base64-encoded 32-byte key)");
    }
    try {
      return Base64.getDecoder().decode(configured);
    } catch (IllegalArgumentException notBase64) {
      // Allow raw UTF-8 in local dev so developers don't need to base64-encode by hand.
      return configured.getBytes(StandardCharsets.UTF_8);
    }
  }
}
