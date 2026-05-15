package io.soqlexplorer.application.ports.salesforce;

import io.soqlexplorer.domain.connection.Environment;
import java.net.URI;
import java.util.Objects;

/**
 * Result of starting an OAuth Web Server Flow.
 *
 * <p>The {@code authorizationUrl} is what the SPA redirects the browser to. {@code state} is the
 * signed nonce we expect to receive back on the callback — the controller checks the signature
 * and the bound environment + ownerId.
 */
public record OAuthAuthorizationRequest(URI authorizationUrl, String state, Environment environment) {

  public OAuthAuthorizationRequest {
    Objects.requireNonNull(authorizationUrl, "authorizationUrl");
    Objects.requireNonNull(state, "state");
    Objects.requireNonNull(environment, "environment");
  }
}
