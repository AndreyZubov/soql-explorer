package io.soqlexplorer.application.auth;

import io.soqlexplorer.application.ports.security.PasswordHasherPort;
import io.soqlexplorer.application.ports.user.UserRepositoryPort;
import io.soqlexplorer.domain.user.Email;
import io.soqlexplorer.domain.user.User;
import java.util.Objects;

/**
 * Default implementation of {@link AuthenticateUserUseCase}.
 *
 * <p>Verifies that:
 *
 * <ul>
 *   <li>a user with the given email exists,
 *   <li>the user is enabled,
 *   <li>the supplied raw password matches the stored hash.
 * </ul>
 *
 * <p>Any failure throws {@link InvalidCredentialsException} with no leakage of which check
 * failed. The service is pure (no Spring annotations) — wiring happens in the web module.
 */
public class AuthenticateUserService implements AuthenticateUserUseCase {

  private final UserRepositoryPort users;
  private final PasswordHasherPort hasher;

  public AuthenticateUserService(UserRepositoryPort users, PasswordHasherPort hasher) {
    this.users = Objects.requireNonNull(users, "users");
    this.hasher = Objects.requireNonNull(hasher, "hasher");
  }

  @Override
  public AuthenticatedUser authenticate(Email email, String rawPassword) {
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(rawPassword, "rawPassword");

    User user = users.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
    if (!user.isEnabled()) {
      throw new InvalidCredentialsException();
    }
    if (!hasher.matches(rawPassword, user.passwordHash())) {
      throw new InvalidCredentialsException();
    }
    return new AuthenticatedUser(user.id(), user.email(), user.roles());
  }
}
