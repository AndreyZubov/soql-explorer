package io.soqlexplorer.domain.user;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Application user aggregate root.
 *
 * <p>This is a pure-Java model: it carries the invariants ("a user always has an email and at
 * least one role") and exposes a small, intentional surface for the application layer. JPA-
 * specific entities live in the persistence module and map to/from this type.
 */
public final class User {

  private final UserId id;
  private final Email email;
  private PasswordHash passwordHash;
  private final Set<Role> roles;
  private final Instant createdAt;
  private Instant updatedAt;
  private boolean enabled;

  private User(
      UserId id,
      Email email,
      PasswordHash passwordHash,
      Set<Role> roles,
      Instant createdAt,
      Instant updatedAt,
      boolean enabled) {
    this.id = Objects.requireNonNull(id, "id");
    this.email = Objects.requireNonNull(email, "email");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.roles = EnumSet.copyOf(Objects.requireNonNull(roles, "roles"));
    if (this.roles.isEmpty()) {
      throw new IllegalArgumentException("A user must have at least one role");
    }
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    this.enabled = enabled;
  }

  /** Factory for newly-registered users: enabled by default, USER role. */
  public static User register(Email email, PasswordHash passwordHash, Instant now) {
    return new User(
        UserId.newId(), email, passwordHash, EnumSet.of(Role.USER), now, now, true);
  }

  /** Rehydrate an existing user from persistence. */
  public static User rehydrate(
      UserId id,
      Email email,
      PasswordHash passwordHash,
      Set<Role> roles,
      Instant createdAt,
      Instant updatedAt,
      boolean enabled) {
    return new User(id, email, passwordHash, roles, createdAt, updatedAt, enabled);
  }

  public void changePassword(PasswordHash newHash, Instant now) {
    this.passwordHash = Objects.requireNonNull(newHash, "newHash");
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public void disable(Instant now) {
    this.enabled = false;
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public UserId id() {
    return id;
  }

  public Email email() {
    return email;
  }

  public PasswordHash passwordHash() {
    return passwordHash;
  }

  public Set<Role> roles() {
    return EnumSet.copyOf(roles);
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
