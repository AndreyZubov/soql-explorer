package io.soqlexplorer.application.connection;

import io.soqlexplorer.application.ports.connection.ConnectionRepositoryPort;
import io.soqlexplorer.application.ports.schema.SchemaCachePort;
import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.user.UserId;
import java.util.Objects;

/** Default implementation of {@link DeleteConnectionUseCase}. */
public class DeleteConnectionService implements DeleteConnectionUseCase {

  private final ConnectionRepositoryPort connections;
  private final SchemaCachePort cache;

  public DeleteConnectionService(ConnectionRepositoryPort connections, SchemaCachePort cache) {
    this.connections = Objects.requireNonNull(connections, "connections");
    this.cache = Objects.requireNonNull(cache, "cache");
  }

  @Override
  public void delete(UserId ownerId, ConnectionId connectionId) {
    Objects.requireNonNull(ownerId, "ownerId");
    Objects.requireNonNull(connectionId, "connectionId");
    connections
        .findByIdForOwner(connectionId, ownerId)
        .orElseThrow(() -> new ConnectionNotFoundException("Connection not found: " + connectionId));
    connections.delete(connectionId);
    cache.invalidate(connectionId);
  }
}
