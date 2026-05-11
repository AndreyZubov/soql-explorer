package io.soqlexplorer.persistence.user;

import static org.assertj.core.api.Assertions.assertThat;

import io.soqlexplorer.application.ports.user.UserRepositoryPort;
import io.soqlexplorer.domain.user.Email;
import io.soqlexplorer.domain.user.PasswordHash;
import io.soqlexplorer.domain.user.User;
import io.soqlexplorer.persistence.AbstractPostgresIntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class UserRepositoryAdapterTest extends AbstractPostgresIntegrationTest {

  @Autowired UserRepositoryPort users;

  @Test
  void saves_and_retrieves_user_by_email() {
    User u =
        User.register(
            Email.of("save.me@example.com"),
            new PasswordHash("$2a$10$abc"),
            Instant.parse("2026-01-01T00:00:00Z"));
    users.save(u);

    assertThat(users.findByEmail(Email.of("save.me@example.com"))).isPresent();
    assertThat(users.existsByEmail(Email.of("SAVE.ME@example.com"))).isTrue();
  }

  @Test
  void update_round_trips_mutable_fields() {
    User u =
        User.register(
            Email.of("update.me@example.com"),
            new PasswordHash("$2a$10$initial"),
            Instant.parse("2026-01-01T00:00:00Z"));
    users.save(u);

    u.changePassword(new PasswordHash("$2a$10$rotated"), Instant.parse("2026-01-02T00:00:00Z"));
    users.save(u);

    User reloaded = users.findById(u.id()).orElseThrow();
    assertThat(reloaded.passwordHash().value()).isEqualTo("$2a$10$rotated");
    assertThat(reloaded.updatedAt()).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"));
  }
}
