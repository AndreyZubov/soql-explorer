package io.soqlexplorer.application.schema;

import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.user.UserId;
import java.util.List;

/** Inbound use case for retrieving the list of sObject API names from a connection. */
public interface ListSObjectsUseCase {

  List<String> list(UserId ownerId, ConnectionId connectionId);
}
