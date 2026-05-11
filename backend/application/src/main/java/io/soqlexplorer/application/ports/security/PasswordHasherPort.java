package io.soqlexplorer.application.ports.security;

import io.soqlexplorer.domain.user.PasswordHash;

/**
 * Outbound port for hashing/verifying user passwords.
 *
 * <p>The application layer must never see, log, or store raw passwords. This port is the only
 * channel through which plaintext credentials cross out of the web layer into the rest of the
 * system; the implementation (BCrypt) lives in {@code web}.
 */
public interface PasswordHasherPort {

  PasswordHash hash(String rawPassword);

  boolean matches(String rawPassword, PasswordHash hash);
}
