package io.soqlexplorer.persistence.connection;

import io.soqlexplorer.application.connection.ConnectionNotFoundException;
import io.soqlexplorer.application.ports.connection.ConnectionRepositoryPort;
import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA-backed adapter implementing {@link ConnectionRepositoryPort}. */
@Component
public class SfConnectionRepositoryAdapter implements ConnectionRepositoryPort {

  private final SfConnectionJpaRepository jpa;

  public SfConnectionRepositoryAdapter(SfConnectionJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<SalesforceConnection> findById(ConnectionId id) {
    return jpa.findById(id.value()).map(SfConnectionMapper::toDomain);
  }

  @Override
  public Optional<SalesforceConnection> findByIdForOwner(ConnectionId id, UserId ownerId) {
    return jpa.findByIdAndOwnerId(id.value(), ownerId.value()).map(SfConnectionMapper::toDomain);
  }

  @Override
  public List<SalesforceConnection> findByOwner(UserId ownerId) {
    return jpa.findAllByOwnerIdOrderByCreatedAtDesc(ownerId.value()).stream()
        .map(SfConnectionMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<SalesforceConnection> findDefaultFor(UserId ownerId) {
    return jpa.findFirstByOwnerIdAndIsDefaultTrue(ownerId.value())
        .map(SfConnectionMapper::toDomain);
  }

  @Override
  @Transactional
  public SalesforceConnection save(SalesforceConnection connection) {
    SfConnectionEntity entity =
        jpa.findById(connection.id().value())
            .map(
                e -> {
                  SfConnectionMapper.copyMutableFields(connection, e);
                  return e;
                })
            .orElseGet(() -> SfConnectionMapper.toEntity(connection));
    return SfConnectionMapper.toDomain(jpa.save(entity));
  }

  @Override
  @Transactional
  public SalesforceConnection markAsDefault(ConnectionId id, UserId ownerId, Instant now) {
    SfConnectionEntity entity =
        jpa.findByIdAndOwnerId(id.value(), ownerId.value())
            .orElseThrow(
                () -> new ConnectionNotFoundException("Connection not found: " + id));
    // The unique partial index requires that no other default exists at commit time. We clear
    // any sibling default first; the flush ordering is handled by Hibernate within the surrounding
    // transaction.
    jpa.clearOtherDefaults(ownerId.value(), id.value(), now);
    entity.setDefault(true);
    entity.setUpdatedAt(now);
    return SfConnectionMapper.toDomain(jpa.save(entity));
  }

  @Override
  public void delete(ConnectionId id) {
    jpa.deleteById(id.value());
  }
}
