package io.soqlexplorer.domain.user;

/**
 * Application roles used to gate authorization decisions.
 *
 * <p>Kept deliberately minimal for v1. The {@link #ADMIN} role guards destructive operations
 * (cache invalidation, audit access). All authenticated users get {@link #USER}.
 */
public enum Role {
  USER,
  ADMIN
}
