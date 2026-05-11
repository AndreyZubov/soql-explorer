package io.soqlexplorer.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.soqlexplorer.application.ports.security.PasswordHasherPort;
import io.soqlexplorer.application.ports.user.UserRepositoryPort;
import io.soqlexplorer.domain.user.Email;
import io.soqlexplorer.domain.user.PasswordHash;
import io.soqlexplorer.domain.user.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {

  @Mock UserRepositoryPort users;
  @Mock PasswordHasherPort hasher;

  @Test
  void authenticates_valid_credentials() {
    AuthenticateUserService svc = new AuthenticateUserService(users, hasher);
    Email email = Email.of("user@example.com");
    PasswordHash hash = new PasswordHash("$2a$10$abc");
    User user = User.register(email, hash, Instant.parse("2026-01-01T00:00:00Z"));

    when(users.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(hasher.matches(eq("pw"), any())).thenReturn(true);

    AuthenticatedUser result = svc.authenticate(email, "pw");

    assertThat(result.email()).isEqualTo(email);
    assertThat(result.id()).isEqualTo(user.id());
  }

  @Test
  void rejects_unknown_user() {
    AuthenticateUserService svc = new AuthenticateUserService(users, hasher);
    when(users.findByEmail(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> svc.authenticate(Email.of("x@y.io"), "pw"))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void rejects_disabled_user() {
    AuthenticateUserService svc = new AuthenticateUserService(users, hasher);
    Email email = Email.of("user@example.com");
    User user = User.register(email, new PasswordHash("x"), Instant.parse("2026-01-01T00:00:00Z"));
    user.disable(Instant.parse("2026-02-01T00:00:00Z"));

    when(users.findByEmail(eq(email))).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> svc.authenticate(email, "pw"))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void rejects_wrong_password() {
    AuthenticateUserService svc = new AuthenticateUserService(users, hasher);
    Email email = Email.of("user@example.com");
    User user = User.register(email, new PasswordHash("x"), Instant.parse("2026-01-01T00:00:00Z"));

    when(users.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(hasher.matches(eq("bad"), any())).thenReturn(false);

    assertThatThrownBy(() -> svc.authenticate(email, "bad"))
        .isInstanceOf(InvalidCredentialsException.class);
  }
}
