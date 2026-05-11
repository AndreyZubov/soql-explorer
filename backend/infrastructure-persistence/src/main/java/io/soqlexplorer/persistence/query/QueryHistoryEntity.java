package io.soqlexplorer.persistence.query;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** JPA mapping for the {@code query_history} table. */
@Entity
@Table(name = "query_history")
public class QueryHistoryEntity {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "connection_id", nullable = false)
  private UUID connectionId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String soql;

  @Column(name = "rows_returned", nullable = false)
  private int rowsReturned;

  @Column(name = "execution_ms", nullable = false)
  private int executionMs;

  @Column(nullable = false, length = 16)
  private String status;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "executed_at", nullable = false)
  private Instant executedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  protected QueryHistoryEntity() {
    // Required by JPA.
  }

  public QueryHistoryEntity(
      UUID id,
      UUID userId,
      UUID connectionId,
      String soql,
      int rowsReturned,
      int executionMs,
      String status,
      String errorMessage,
      Instant executedAt) {
    this.id = id;
    this.userId = userId;
    this.connectionId = connectionId;
    this.soql = soql;
    this.rowsReturned = rowsReturned;
    this.executionMs = executionMs;
    this.status = status;
    this.errorMessage = errorMessage;
    this.executedAt = executedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getConnectionId() {
    return connectionId;
  }

  public String getSoql() {
    return soql;
  }

  public int getRowsReturned() {
    return rowsReturned;
  }

  public int getExecutionMs() {
    return executionMs;
  }

  public String getStatus() {
    return status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Instant getExecutedAt() {
    return executedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }
}
