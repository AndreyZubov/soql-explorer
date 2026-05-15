package io.soqlexplorer.application.connection;

import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.user.UserId;
import java.util.List;

/** Inbound use case for listing all connections owned by a user. */
public interface ListConnectionsUseCase {

  List<SalesforceConnection> list(UserId ownerId);
}
