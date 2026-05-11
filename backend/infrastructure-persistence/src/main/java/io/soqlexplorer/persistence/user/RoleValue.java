package io.soqlexplorer.persistence.user;

/**
 * Mirror of {@code io.soqlexplorer.domain.user.Role} as a persistence-layer enum.
 *
 * <p>We keep a separate enum here so the JPA mapping never leaks into the domain layer. The
 * mapper in {@code UserMapper} round-trips between the two.
 */
public enum RoleValue {
  USER,
  ADMIN
}
