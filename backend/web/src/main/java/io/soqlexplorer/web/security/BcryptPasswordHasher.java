package io.soqlexplorer.web.security;

import io.soqlexplorer.application.ports.security.PasswordHasherPort;
import io.soqlexplorer.domain.user.PasswordHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt-backed implementation of {@link PasswordHasherPort}.
 *
 * <p>Cost factor 12 is the sweet spot in 2026: noticeable on a hot path but invisible to a
 * single end-user. Adjust if/when the auth load profile changes.
 */
@Component
public class BcryptPasswordHasher implements PasswordHasherPort {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

  @Override
  public PasswordHash hash(String rawPassword) {
    return new PasswordHash(encoder.encode(rawPassword));
  }

  @Override
  public boolean matches(String rawPassword, PasswordHash hash) {
    return encoder.matches(rawPassword, hash.value());
  }
}
