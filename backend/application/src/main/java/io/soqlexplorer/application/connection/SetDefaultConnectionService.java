package io.soqlexplorer.application.connection;

import io.soqlexplorer.application.ports.clock.ClockPort;
import io.soqlexplorer.application.ports.connection.ConnectionRepositoryPort;
import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.user.UserId;
import java.util.Objects;

/** Default implementation of {@link SetDefaultConnectionUseCase}. */
public class SetDefaultConnectionService implements SetDefaultConnectionUseCase {

  private final ConnectionRepositoryPort connections;
  private final ClockPort clock;

  public SetDefaultConnectionService(ConnectionRepositoryPort connections, ClockPort clock) {
    this.connections = Objects.requireNonNull(connections, "connections");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  @Override
  public SalesforceConnection setDefault(UserId ownerId, ConnectionId connectionId) {
    Objects.requireNonNull(ownerId, "ownerId");
    Objects.requireNonNull(connectionId, "connectionId");
    connections
        .findByIdForOwner(connectionId, ownerId)
        .orElseThrow(() -> new ConnectionNotFoundException("Connection not found: " + connectionId));
    return connections.markAsDefault(connectionId, ownerId, clock.now());
  }
}
