package io.soqlexplorer.domain.connection;

import java.util.Objects;
import java.util.UUID;

/** Strongly-typed identifier for a {@code SalesforceConnection}. */
public record ConnectionId(UUID value) {

  public ConnectionId {
    Objects.requireNonNull(value, "ConnectionId value must not be null");
  }

  public static ConnectionId newId() {
    return new ConnectionId(UUID.randomUUID());
  }

  public static ConnectionId of(UUID value) {
    return new ConnectionId(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
