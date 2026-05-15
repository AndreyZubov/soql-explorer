package io.soqlexplorer.application.connection;

import io.soqlexplorer.application.ports.connection.ConnectionRepositoryPort;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.user.UserId;
import java.util.List;
import java.util.Objects;

/** Default implementation of {@link ListConnectionsUseCase}. */
public class ListConnectionsService implements ListConnectionsUseCase {

  private final ConnectionRepositoryPort connections;

  public ListConnectionsService(ConnectionRepositoryPort connections) {
    this.connections = Objects.requireNonNull(connections, "connections");
  }

  @Override
  public List<SalesforceConnection> list(UserId ownerId) {
    Objects.requireNonNull(ownerId, "ownerId");
    return connections.findByOwner(ownerId);
  }
}
