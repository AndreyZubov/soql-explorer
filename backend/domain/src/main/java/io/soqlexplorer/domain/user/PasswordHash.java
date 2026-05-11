package io.soqlexplorer.domain.user;

import java.util.Objects;

/**
 * Opaque value object that wraps an already-hashed password.
 *
 * <p>The domain layer never sees the raw password; hashing is performed by an infrastructure
 * adapter (BCrypt) before a {@link User} aggregate is constructed. Keeping the type separate
 * makes it impossible to accidentally pass a plaintext string where a hash is required.
 */
public record PasswordHash(String value) {

  public PasswordHash {
    Objects.requireNonNull(value, "PasswordHash value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("PasswordHash must not be blank");
    }
  }

  @Override
  public String toString() {
    // Never expose the hash itself in logs or stack traces.
    return "PasswordHash[REDACTED]";
  }
}
