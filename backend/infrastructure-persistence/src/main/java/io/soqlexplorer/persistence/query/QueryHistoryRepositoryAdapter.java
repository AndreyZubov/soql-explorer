package io.soqlexplorer.persistence.query;

import io.soqlexplorer.application.ports.clock.ClockPort;
import io.soqlexplorer.application.ports.query.QueryHistoryRepositoryPort;
import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.query.QueryHistoryEntry;
import io.soqlexplorer.domain.query.SoqlQuery;
import io.soqlexplorer.domain.user.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class QueryHistoryRepositoryAdapter implements QueryHistoryRepositoryPort {

  private final QueryHistoryJpaRepository jpa;
  private final ClockPort clock;

  public QueryHistoryRepositoryAdapter(QueryHistoryJpaRepository jpa, ClockPort clock) {
    this.jpa = jpa;
    this.clock = clock;
  }

  @Override
  public QueryHistoryEntry save(QueryHistoryEntry entry) {
    QueryHistoryEntity entity =
        new QueryHistoryEntity(
            entry.id(),
            entry.userId().value(),
            entry.connectionId().value(),
            entry.soql().text(),
            entry.rowsReturned(),
            (int) Math.min(entry.executionTime().toMillis(), Integer.MAX_VALUE),
            entry.status().name(),
            entry.errorMessage(),
            entry.executedAt());
    QueryHistoryEntity saved = jpa.save(entity);
    return toDomain(saved);
  }

  @Override
  public List<QueryHistoryEntry> findRecentForUser(UserId userId, int limit) {
    return jpa.findRecentForUser(userId.value(), PageRequest.of(0, limit)).stream()
        .map(QueryHistoryRepositoryAdapter::toDomain)
        .toList();
  }

  @Override
  @Transactional
  public void deleteById(UUID id, UserId requestingUserId) {
    jpa.softDeleteForUser(id, requestingUserId.value(), clock.now());
  }

  @Override
  @Transactional
  public int softDeleteOlderThan(Instant cutoff) {
    return jpa.softDeleteOlderThan(cutoff, clock.now());
  }

  private static QueryHistoryEntry toDomain(QueryHistoryEntity e) {
    return new QueryHistoryEntry(
        e.getId(),
        UserId.of(e.getUserId()),
        ConnectionId.of(e.getConnectionId()),
        SoqlQuery.of(e.getSoql()),
        e.getRowsReturned(),
        Duration.ofMillis(e.getExecutionMs()),
        QueryHistoryEntry.ExecutionStatus.valueOf(e.getStatus()),
        e.getErrorMessage(),
        e.getExecutedAt());
  }
}
