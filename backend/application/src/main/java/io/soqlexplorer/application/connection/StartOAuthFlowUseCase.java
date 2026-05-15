package io.soqlexplorer.application.connection;

import io.soqlexplorer.application.ports.salesforce.OAuthAuthorizationRequest;
import io.soqlexplorer.domain.connection.Environment;
import io.soqlexplorer.domain.user.UserId;

/** Inbound use case for kicking off a Salesforce OAuth Web Server Flow. */
public interface StartOAuthFlowUseCase {

  OAuthAuthorizationRequest start(UserId ownerId, Environment environment);
}
