package io.soqlexplorer.salesforce.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.soqlexplorer.domain.connection.EncryptedToken;
import io.soqlexplorer.salesforce.SalesforceProperties;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class AesGcmTokenCipherTest {

  private static SalesforceProperties propsWithKey(byte[] key) {
    SalesforceProperties props = new SalesforceProperties();
    props.setRefreshTokenEncryptionKey(Base64.getEncoder().encodeToString(key));
    return props;
  }

  @Test
  void round_trip_recovers_plaintext() {
    byte[] key = new byte[32];
    for (int i = 0; i < key.length; i++) {
      key[i] = (byte) i;
    }
    AesGcmTokenCipher cipher = new AesGcmTokenCipher(propsWithKey(key));

    EncryptedToken encrypted = cipher.encrypt("super-secret-refresh-token");

    assertThat(cipher.decrypt(encrypted)).isEqualTo("super-secret-refresh-token");
  }

  @Test
  void encryption_produces_unique_output_per_call() {
    byte[] key = new byte[32];
    AesGcmTokenCipher cipher = new AesGcmTokenCipher(propsWithKey(key));

    EncryptedToken a = cipher.encrypt("same-plaintext");
    EncryptedToken b = cipher.encrypt("same-plaintext");

    assertThat(a.ciphertext()).isNotEqualTo(b.ciphertext());
    assertThat(cipher.decrypt(a)).isEqualTo(cipher.decrypt(b));
  }

  @Test
  void short_key_is_rejected() {
    SalesforceProperties props = new SalesforceProperties();
    props.setRefreshTokenEncryptionKey(Base64.getEncoder().encodeToString(new byte[16]));

    assertThatThrownBy(() -> new AesGcmTokenCipher(props))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("32 bytes");
  }
}
