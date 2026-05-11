package io.soqlexplorer.domain.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for an application user.
 *
 * <p>Wrapping the UUID prevents accidental mixing of identifiers from different aggregates
 * (a frequent source of subtle bugs in services that pass raw UUIDs around).
 */
public record UserId(UUID value) {

  public UserId {
    Objects.requireNonNull(value, "UserId value must not be null");
  }

  public static UserId newId() {
    return new UserId(UUID.randomUUID());
  }

  public static UserId of(UUID value) {
    return new UserId(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
