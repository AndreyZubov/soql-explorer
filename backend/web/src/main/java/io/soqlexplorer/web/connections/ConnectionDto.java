package io.soqlexplorer.web.connections;

import io.soqlexplorer.domain.connection.SalesforceConnection;
import java.time.Instant;
import java.util.UUID;

/**
 * Outbound projection of a Salesforce connection.
 *
 * <p>Deliberately omits the encrypted refresh token — it should never leave the backend.
 */
public record ConnectionDto(
    UUID id,
    String orgId,
    String instanceUrl,
    String environment,
    String displayName,
    boolean isDefault,
    Instant createdAt,
    Instant updatedAt) {

  public static ConnectionDto from(SalesforceConnection c) {
    return new ConnectionDto(
        c.id().value(),
        c.orgId().value(),
        c.instanceUrl().toString(),
        c.environment().name(),
        c.displayName(),
        c.isDefault(),
        c.createdAt(),
        c.updatedAt());
  }
}
