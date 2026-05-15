package io.soqlexplorer.application.schema;

import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.user.UserId;
import java.util.Map;

/** Inbound use case for fetching the field/picklist/relationship metadata of an sObject. */
public interface DescribeSObjectUseCase {

  Map<String, Object> describe(UserId ownerId, ConnectionId connectionId, String sObjectName);
}
