package io.soqlexplorer.persistence.query;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA mapping for the {@code query_template} table.
 *
 * <p>Templates are versioned: the composite key is {@code (id, version)}. The {@link
 * QueryTemplateEntity.Key} class fulfills the JPA composite-id contract.
 */
@Entity
@Table(name = "query_template")
@IdClass(QueryTemplateEntity.Key.class)
public class QueryTemplateEntity {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Id
  @Column(nullable = false, updatable = false)
  private int version;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String soql;

  // PostgreSQL TEXT[] stored as a comma-separated string at the JPA level for portability
  // with H2/Testcontainers. The application layer treats this as a sorted, lower-cased set.
  @Column(nullable = false, columnDefinition = "TEXT")
  private String tags;

  @Column(nullable = false)
  private boolean shared;

  @Column(name = "is_latest", nullable = false)
  private boolean isLatest;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected QueryTemplateEntity() {
    // Required by JPA.
  }

  public QueryTemplateEntity(
      UUID id,
      int version,
      UUID ownerId,
      String name,
      String soql,
      String tags,
      boolean shared,
      boolean isLatest,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.version = version;
    this.ownerId = ownerId;
    this.name = name;
    this.soql = soql;
    this.tags = tags;
    this.shared = shared;
    this.isLatest = isLatest;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSoql() {
    return soql;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public boolean isShared() {
    return shared;
  }

  public void setShared(boolean shared) {
    this.shared = shared;
  }

  public boolean isLatest() {
    return isLatest;
  }

  public void setLatest(boolean isLatest) {
    this.isLatest = isLatest;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  /** Composite primary key for {@link QueryTemplateEntity}. */
  public static class Key implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private int version;

    public Key() {}

    public Key(UUID id, int version) {
      this.id = id;
      this.version = version;
    }

    public UUID getId() {
      return id;
    }

    public int getVersion() {
      return version;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Key key)) return false;
      return version == key.version && Objects.equals(id, key.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, version);
    }
  }
}
