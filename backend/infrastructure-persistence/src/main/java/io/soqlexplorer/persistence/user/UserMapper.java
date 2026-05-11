package io.soqlexplorer.persistence.user;

import io.soqlexplorer.domain.user.Email;
import io.soqlexplorer.domain.user.PasswordHash;
import io.soqlexplorer.domain.user.Role;
import io.soqlexplorer.domain.user.User;
import io.soqlexplorer.domain.user.UserId;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Pure mapper between {@link User} (domain) and {@link UserEntity} (JPA). */
final class UserMapper {

  private UserMapper() {
    // utility class
  }

  static User toDomain(UserEntity entity) {
    Set<Role> roles =
        entity.getRoles().stream()
            .map(rv -> Role.valueOf(rv.name()))
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
    return User.rehydrate(
        UserId.of(entity.getId()),
        Email.of(entity.getEmail()),
        new PasswordHash(entity.getPasswordHash()),
        roles,
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.isEnabled());
  }

  static UserEntity toEntity(User user) {
    Set<RoleValue> roleValues =
        user.roles().stream().map(r -> RoleValue.valueOf(r.name())).collect(Collectors.toSet());
    return new UserEntity(
        user.id().value(),
        user.email().value(),
        user.passwordHash().value(),
        user.isEnabled(),
        user.createdAt(),
        user.updatedAt(),
        roleValues);
  }

  static void copyMutableFields(User source, UserEntity target) {
    target.setPasswordHash(source.passwordHash().value());
    target.setEnabled(source.isEnabled());
    target.setUpdatedAt(source.updatedAt());
    target.setRoles(
        source.roles().stream().map(r -> RoleValue.valueOf(r.name())).collect(Collectors.toSet()));
  }
}
