package io.soqlexplorer.application.ports.connection;

import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.user.UserId;
import java.util.List;
import java.util.Optional;

/** Outbound port for {@link SalesforceConnection} persistence. */
public interface ConnectionRepositoryPort {

  Optional<SalesforceConnection> findById(ConnectionId id);

  /**
   * Returns the connection belonging to the supplied owner, if any. Used by every use case that
   * needs to enforce ownership before reading or mutating a connection.
   */
  Optional<SalesforceConnection> findByIdForOwner(ConnectionId id, UserId ownerId);

  List<SalesforceConnection> findByOwner(UserId ownerId);

  Optional<SalesforceConnection> findDefaultFor(UserId ownerId);

  SalesforceConnection save(SalesforceConnection connection);

  /**
   * Atomically marks the supplied connection as default for its owner — clearing any other
   * default the owner may have. The DB-level partial unique index guarantees at most one default
   * per owner; this method exists so the adapter can wrap both updates in a single transaction.
   */
  SalesforceConnection markAsDefault(ConnectionId id, UserId ownerId, java.time.Instant now);

  void delete(ConnectionId id);
}
