package io.soqlexplorer.application.schema;

import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.user.UserId;
import java.util.Map;

/** Inbound use case for retrieving relationships (childRelationships + reference fields) of an sObject. */
public interface GetRelationshipsUseCase {

  Map<String, Object> get(UserId ownerId, ConnectionId connectionId, String sObjectName);
}
