package io.soqlexplorer.application.connection;

import io.soqlexplorer.application.ports.clock.ClockPort;
import io.soqlexplorer.application.ports.connection.ConnectionRepositoryPort;
import io.soqlexplorer.application.ports.salesforce.OAuthExchangeResult;
import io.soqlexplorer.application.ports.salesforce.SalesforceOAuthPort;
import io.soqlexplorer.application.ports.security.TokenCipherPort;
import io.soqlexplorer.domain.connection.EncryptedToken;
import io.soqlexplorer.domain.connection.Environment;
import io.soqlexplorer.domain.connection.OrgId;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import java.util.Objects;

/** Default implementation of {@link HandleOAuthCallbackUseCase}. */
public class HandleOAuthCallbackService implements HandleOAuthCallbackUseCase {

  private final SalesforceOAuthPort oauth;
  private final ConnectionRepositoryPort connections;
  private final TokenCipherPort cipher;
  private final ClockPort clock;

  public HandleOAuthCallbackService(
      SalesforceOAuthPort oauth,
      ConnectionRepositoryPort connections,
      TokenCipherPort cipher,
      ClockPort clock) {
    this.oauth = Objects.requireNonNull(oauth, "oauth");
    this.connections = Objects.requireNonNull(connections, "connections");
    this.cipher = Objects.requireNonNull(cipher, "cipher");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  @Override
  public SalesforceConnection complete(String code, String state) {
    Objects.requireNonNull(code, "code");
    Objects.requireNonNull(state, "state");

    OAuthExchangeResult result = oauth.exchangeAuthorizationCode(code, state);
    EncryptedToken refreshToken = cipher.encrypt(result.refreshToken());
    OrgId orgId = OrgId.of(result.orgId());
    String displayName = deriveDisplayName(result.environment(), result);

    SalesforceConnection connection =
        SalesforceConnection.create(
            result.ownerId(),
            orgId,
            result.instanceUrl(),
            result.environment(),
            displayName,
            refreshToken,
            clock.now());
    return connections.save(connection);
  }

  private static String deriveDisplayName(Environment environment, OAuthExchangeResult result) {
    String host = result.instanceUrl().getHost();
    return (environment == Environment.SANDBOX ? "Sandbox · " : "Production · ") + host;
  }
}
