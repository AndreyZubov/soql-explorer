package io.soqlexplorer.persistence.user;

import io.soqlexplorer.application.ports.user.UserRepositoryPort;
import io.soqlexplorer.domain.user.Email;
import io.soqlexplorer.domain.user.User;
import io.soqlexplorer.domain.user.UserId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Spring-managed adapter that fulfills {@link UserRepositoryPort} using {@link
 * UserJpaRepository}.
 *
 * <p>The adapter handles upsert semantics: if the user already exists, we copy mutable fields
 * onto the loaded entity instead of overwriting it, so JPA's dirty-checking does the right thing
 * within the surrounding transaction.
 */
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

  private final UserJpaRepository jpa;

  public UserRepositoryAdapter(UserJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<User> findById(UserId id) {
    return jpa.findById(id.value()).map(UserMapper::toDomain);
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return jpa.findByEmailIgnoreCase(email.value()).map(UserMapper::toDomain);
  }

  @Override
  public User save(User user) {
    UserEntity entity =
        jpa.findById(user.id().value())
            .map(
                e -> {
                  UserMapper.copyMutableFields(user, e);
                  return e;
                })
            .orElseGet(() -> UserMapper.toEntity(user));
    UserEntity saved = jpa.save(entity);
    return UserMapper.toDomain(saved);
  }

  @Override
  public boolean existsByEmail(Email email) {
    return jpa.existsByEmailIgnoreCase(email.value());
  }
}
