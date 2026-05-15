package io.soqlexplorer.application.schema;

import io.soqlexplorer.application.connection.ConnectionNotFoundException;
import io.soqlexplorer.application.ports.connection.ConnectionRepositoryPort;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayPort;
import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.user.UserId;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link GetRelationshipsUseCase}. Backed by the existing describe call
 * because Salesforce returns relationships as part of the describe payload — we just project a
 * subset of it for clients that don't want the whole metadata blob.
 */
public class GetRelationshipsService implements GetRelationshipsUseCase {

  private final ConnectionRepositoryPort connections;
  private final SalesforceGatewayPort gateway;

  public GetRelationshipsService(
      ConnectionRepositoryPort connections, SalesforceGatewayPort gateway) {
    this.connections = Objects.requireNonNull(connections, "connections");
    this.gateway = Objects.requireNonNull(gateway, "gateway");
  }

  @Override
  public Map<String, Object> get(UserId ownerId, ConnectionId connectionId, String sObjectName) {
    if (sObjectName == null || sObjectName.isBlank()) {
      throw new IllegalArgumentException("sObjectName must not be blank");
    }
    SalesforceConnection connection =
        connections
            .findByIdForOwner(connectionId, ownerId)
            .orElseThrow(
                () -> new ConnectionNotFoundException("Connection not found: " + connectionId));
    return gateway.getRelationships(connection, sObjectName);
  }
}
