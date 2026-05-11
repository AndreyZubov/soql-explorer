package io.soqlexplorer.domain.connection;

import io.soqlexplorer.domain.user.UserId;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;

/**
 * A Salesforce organization that an application user has authorized us to query.
 *
 * <p>Invariants enforced by the aggregate:
 *
 * <ul>
 *   <li>A connection always belongs to exactly one application user (multi-tenant SaaS is out of
 *       scope for v1).
 *   <li>The encrypted refresh token is the only credential persisted; access tokens are derived
 *       on demand and cached in memory only.
 *   <li>{@code instanceUrl} is what we hit for REST calls; it is independent of the OAuth login
 *       host (production vs sandbox).
 * </ul>
 */
public final class SalesforceConnection {

  private final ConnectionId id;
  private final UserId ownerId;
  private final OrgId orgId;
  private final URI instanceUrl;
  private final Environment environment;
  private String displayName;
  private EncryptedToken refreshToken;
  private boolean defaultConnection;
  private final Instant createdAt;
  private Instant updatedAt;

  private SalesforceConnection(
      ConnectionId id,
      UserId ownerId,
      OrgId orgId,
      URI instanceUrl,
      Environment environment,
      String displayName,
      EncryptedToken refreshToken,
      boolean defaultConnection,
      Instant createdAt,
      Instant updatedAt) {
    this.id = Objects.requireNonNull(id, "id");
    this.ownerId = Objects.requireNonNull(ownerId, "ownerId");
    this.orgId = Objects.requireNonNull(orgId, "orgId");
    this.instanceUrl = Objects.requireNonNull(instanceUrl, "instanceUrl");
    this.environment = Objects.requireNonNull(environment, "environment");
    this.displayName = requireNonBlank(displayName, "displayName");
    this.refreshToken = Objects.requireNonNull(refreshToken, "refreshToken");
    this.defaultConnection = defaultConnection;
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
  }

  public static SalesforceConnection create(
      UserId ownerId,
      OrgId orgId,
      URI instanceUrl,
      Environment environment,
      String displayName,
      EncryptedToken refreshToken,
      Instant now) {
    return new SalesforceConnection(
        ConnectionId.newId(),
        ownerId,
        orgId,
        instanceUrl,
        environment,
        displayName,
        refreshToken,
        false,
        now,
        now);
  }

  public static SalesforceConnection rehydrate(
      ConnectionId id,
      UserId ownerId,
      OrgId orgId,
      URI instanceUrl,
      Environment environment,
      String displayName,
      EncryptedToken refreshToken,
      boolean defaultConnection,
      Instant createdAt,
      Instant updatedAt) {
    return new SalesforceConnection(
        id,
        ownerId,
        orgId,
        instanceUrl,
        environment,
        displayName,
        refreshToken,
        defaultConnection,
        createdAt,
        updatedAt);
  }

  public void rename(String newDisplayName, Instant now) {
    this.displayName = requireNonBlank(newDisplayName, "displayName");
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public void rotateRefreshToken(EncryptedToken newToken, Instant now) {
    this.refreshToken = Objects.requireNonNull(newToken, "newToken");
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public void markDefault(Instant now) {
    this.defaultConnection = true;
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public void unmarkDefault(Instant now) {
    this.defaultConnection = false;
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public ConnectionId id() {
    return id;
  }

  public UserId ownerId() {
    return ownerId;
  }

  public OrgId orgId() {
    return orgId;
  }

  public URI instanceUrl() {
    return instanceUrl;
  }

  public Environment environment() {
    return environment;
  }

  public String displayName() {
    return displayName;
  }

  public EncryptedToken refreshToken() {
    return refreshToken;
  }

  public boolean isDefault() {
    return defaultConnection;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  private static String requireNonBlank(String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SalesforceConnection other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
