package io.soqlexplorer.persistence.connection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** JPA mapping for the {@code sf_connection} table. */
@Entity
@Table(name = "sf_connection")
public class SfConnectionEntity {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @Column(name = "org_id", nullable = false, length = 18)
  private String orgId;

  @Column(name = "instance_url", nullable = false, length = 512)
  private String instanceUrl;

  @Column(nullable = false, length = 16)
  private String environment;

  @Column(name = "display_name", nullable = false, length = 128)
  private String displayName;

  @Column(name = "refresh_token", nullable = false, columnDefinition = "BYTEA")
  private byte[] refreshToken;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected SfConnectionEntity() {
    // Required by JPA.
  }

  public SfConnectionEntity(
      UUID id,
      UUID ownerId,
      String orgId,
      String instanceUrl,
      String environment,
      String displayName,
      byte[] refreshToken,
      boolean isDefault,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.ownerId = ownerId;
    this.orgId = orgId;
    this.instanceUrl = instanceUrl;
    this.environment = environment;
    this.displayName = displayName;
    this.refreshToken = refreshToken;
    this.isDefault = isDefault;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public String getOrgId() {
    return orgId;
  }

  public String getInstanceUrl() {
    return instanceUrl;
  }

  public String getEnvironment() {
    return environment;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public byte[] getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(byte[] refreshToken) {
    this.refreshToken = refreshToken;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
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
}
