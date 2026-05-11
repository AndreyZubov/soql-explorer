package io.soqlexplorer.application.ports.user;

import io.soqlexplorer.domain.user.Email;
import io.soqlexplorer.domain.user.User;
import io.soqlexplorer.domain.user.UserId;
import java.util.Optional;

/** Outbound port for persisting and retrieving {@link User} aggregates. */
public interface UserRepositoryPort {

  Optional<User> findById(UserId id);

  Optional<User> findByEmail(Email email);

  User save(User user);

  boolean existsByEmail(Email email);
}
