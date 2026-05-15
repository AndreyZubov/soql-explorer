package io.soqlexplorer.application.connection;

import io.soqlexplorer.application.ports.salesforce.OAuthAuthorizationRequest;
import io.soqlexplorer.application.ports.salesforce.SalesforceOAuthPort;
import io.soqlexplorer.domain.connection.Environment;
import io.soqlexplorer.domain.user.UserId;
import java.util.Objects;

/** Default implementation of {@link StartOAuthFlowUseCase}. */
public class StartOAuthFlowService implements StartOAuthFlowUseCase {

  private final SalesforceOAuthPort oauth;

  public StartOAuthFlowService(SalesforceOAuthPort oauth) {
    this.oauth = Objects.requireNonNull(oauth, "oauth");
  }

  @Override
  public OAuthAuthorizationRequest start(UserId ownerId, Environment environment) {
    Objects.requireNonNull(ownerId, "ownerId");
    Objects.requireNonNull(environment, "environment");
    return oauth.startAuthorization(ownerId, environment);
  }
}
