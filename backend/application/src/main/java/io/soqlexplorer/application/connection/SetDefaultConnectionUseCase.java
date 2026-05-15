package io.soqlexplorer.application.connection;

import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.user.UserId;

/** Inbound use case for marking one of the user's connections as their default. */
public interface SetDefaultConnectionUseCase {

  SalesforceConnection setDefault(UserId ownerId, ConnectionId connectionId);
}
