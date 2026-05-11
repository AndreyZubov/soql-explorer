package io.soqlexplorer.domain.connection;

import java.util.Arrays;
import java.util.Objects;

/**
 * Opaque envelope for an encrypted Salesforce refresh token.
 *
 * <p>The domain layer treats refresh tokens as opaque byte blobs. Encryption/decryption is
 * performed by an infrastructure adapter (see {@code TokenCipherPort}). Storing the ciphertext as
 * a byte array — not a {@link String} — keeps it out of accidental {@code toString()}/log output
 * and avoids charset round-tripping bugs.
 */
public final class EncryptedToken {

  private final byte[] ciphertext;

  public EncryptedToken(byte[] ciphertext) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    if (ciphertext.length == 0) {
      throw new IllegalArgumentException("ciphertext must not be empty");
    }
    // Defensive copy: callers may reuse buffers.
    this.ciphertext = ciphertext.clone();
  }

  public byte[] ciphertext() {
    return ciphertext.clone();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EncryptedToken other)) return false;
    return Arrays.equals(ciphertext, other.ciphertext);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(ciphertext);
  }

  @Override
  public String toString() {
    return "EncryptedToken[REDACTED, length=" + ciphertext.length + "]";
  }
}
