package io.soqlexplorer.application.ports.connection;

import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.user.UserId;
import java.util.List;
import java.util.Optional;

/** Outbound port for {@link SalesforceConnection} persistence. */
public interface ConnectionRepositoryPort {

  Optional<SalesforceConnection> findById(ConnectionId id);

  List<SalesforceConnection> findByOwner(UserId ownerId);

  Optional<SalesforceConnection> findDefaultFor(UserId ownerId);

  SalesforceConnection save(SalesforceConnection connection);

  void delete(ConnectionId id);
}
