package io.soqlexplorer.domain.query;

import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.user.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Audit record of a single executed SOQL query.
 *
 * <p>History entries are immutable after creation; they exist solely to let a user re-run or
 * inspect past queries and to feed the analytics dashboards mentioned in Step 5. A scheduled job
 * (Step 3) soft-deletes entries older than 90 days.
 */
public record QueryHistoryEntry(
    UUID id,
    UserId userId,
    ConnectionId connectionId,
    SoqlQuery soql,
    int rowsReturned,
    Duration executionTime,
    ExecutionStatus status,
    String errorMessage,
    Instant executedAt) {

  public QueryHistoryEntry {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(connectionId, "connectionId");
    Objects.requireNonNull(soql, "soql");
    Objects.requireNonNull(executionTime, "executionTime");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(executedAt, "executedAt");
    if (rowsReturned < 0) {
      throw new IllegalArgumentException("rowsReturned must be >= 0");
    }
    if (executionTime.isNegative()) {
      throw new IllegalArgumentException("executionTime must be >= 0");
    }
    if (status == ExecutionStatus.FAILURE && (errorMessage == null || errorMessage.isBlank())) {
      throw new IllegalArgumentException("Failed entries must include an errorMessage");
    }
  }

  public static QueryHistoryEntry success(
      UserId userId,
      ConnectionId connectionId,
      SoqlQuery soql,
      int rowsReturned,
      Duration executionTime,
      Instant executedAt) {
    return new QueryHistoryEntry(
        UUID.randomUUID(),
        userId,
        connectionId,
        soql,
        rowsReturned,
        executionTime,
        ExecutionStatus.SUCCESS,
        null,
        executedAt);
  }

  public static QueryHistoryEntry failure(
      UserId userId,
      ConnectionId connectionId,
      SoqlQuery soql,
      Duration executionTime,
      String errorMessage,
      Instant executedAt) {
    return new QueryHistoryEntry(
        UUID.randomUUID(),
        userId,
        connectionId,
        soql,
        0,
        executionTime,
        ExecutionStatus.FAILURE,
        errorMessage,
        executedAt);
  }

  public enum ExecutionStatus {
    SUCCESS,
    FAILURE
  }
}
