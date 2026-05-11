package io.soqlexplorer.application.ports.query;

import io.soqlexplorer.domain.query.QueryHistoryEntry;
import io.soqlexplorer.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Outbound port for persisting and retrieving {@link QueryHistoryEntry} records. */
public interface QueryHistoryRepositoryPort {

  QueryHistoryEntry save(QueryHistoryEntry entry);

  List<QueryHistoryEntry> findRecentForUser(UserId userId, int limit);

  void deleteById(UUID id, UserId requestingUserId);

  /** Soft-deletes entries older than the cutoff. Used by the 90-day retention job. */
  int softDeleteOlderThan(Instant cutoff);
}
