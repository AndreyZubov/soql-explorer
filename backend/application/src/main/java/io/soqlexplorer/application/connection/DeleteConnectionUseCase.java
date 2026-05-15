package io.soqlexplorer.application.connection;

import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.user.UserId;

/** Inbound use case for removing a user's Salesforce connection. */
public interface DeleteConnectionUseCase {

  void delete(UserId ownerId, ConnectionId connectionId);
}
