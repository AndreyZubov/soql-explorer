package io.soqlexplorer.application.schema;

import io.soqlexplorer.application.connection.ConnectionNotFoundException;
import io.soqlexplorer.application.ports.connection.ConnectionRepositoryPort;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayPort;
import io.soqlexplorer.application.ports.schema.SchemaCachePort;
import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.user.UserId;
import java.util.Map;
import java.util.Objects;

/** Default implementation of {@link DescribeSObjectUseCase}. */
public class DescribeSObjectService implements DescribeSObjectUseCase {

  private final ConnectionRepositoryPort connections;
  private final SalesforceGatewayPort gateway;
  private final SchemaCachePort cache;

  public DescribeSObjectService(
      ConnectionRepositoryPort connections, SalesforceGatewayPort gateway, SchemaCachePort cache) {
    this.connections = Objects.requireNonNull(connections, "connections");
    this.gateway = Objects.requireNonNull(gateway, "gateway");
    this.cache = Objects.requireNonNull(cache, "cache");
  }

  @Override
  public Map<String, Object> describe(
      UserId ownerId, ConnectionId connectionId, String sObjectName) {
    if (sObjectName == null || sObjectName.isBlank()) {
      throw new IllegalArgumentException("sObjectName must not be blank");
    }
    SalesforceConnection connection =
        connections
            .findByIdForOwner(connectionId, ownerId)
            .orElseThrow(
                () -> new ConnectionNotFoundException("Connection not found: " + connectionId));
    return cache.describe(
        connectionId, sObjectName, () -> gateway.describeSObject(connection, sObjectName));
  }
}
