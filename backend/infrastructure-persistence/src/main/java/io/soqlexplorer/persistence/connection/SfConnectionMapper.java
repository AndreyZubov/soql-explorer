package io.soqlexplorer.persistence.connection;

import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.connection.EncryptedToken;
import io.soqlexplorer.domain.connection.Environment;
import io.soqlexplorer.domain.connection.OrgId;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.user.UserId;
import java.net.URI;

final class SfConnectionMapper {

  private SfConnectionMapper() {
    // utility class
  }

  static SalesforceConnection toDomain(SfConnectionEntity e) {
    return SalesforceConnection.rehydrate(
        ConnectionId.of(e.getId()),
        UserId.of(e.getOwnerId()),
        OrgId.of(e.getOrgId()),
        URI.create(e.getInstanceUrl()),
        Environment.valueOf(e.getEnvironment()),
        e.getDisplayName(),
        new EncryptedToken(e.getRefreshToken()),
        e.isDefault(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }

  static SfConnectionEntity toEntity(SalesforceConnection c) {
    return new SfConnectionEntity(
        c.id().value(),
        c.ownerId().value(),
        c.orgId().value(),
        c.instanceUrl().toString(),
        c.environment().name(),
        c.displayName(),
        c.refreshToken().ciphertext(),
        c.isDefault(),
        c.createdAt(),
        c.updatedAt());
  }

  static void copyMutableFields(SalesforceConnection source, SfConnectionEntity target) {
    target.setDisplayName(source.displayName());
    target.setRefreshToken(source.refreshToken().ciphertext());
    target.setDefault(source.isDefault());
    target.setUpdatedAt(source.updatedAt());
  }
}
