package io.soqlexplorer.web.connections;

import io.soqlexplorer.application.connection.HandleOAuthCallbackUseCase;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayException;
import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles the post-consent redirect from Salesforce.
 *
 * <p>This endpoint is reached by the user's browser, not by the SPA, so it must be on the
 * security allow-list. The signed {@code state} parameter binds the request to the original
 * application user — verification happens inside {@link HandleOAuthCallbackUseCase}.
 *
 * <p>On success we redirect the browser back to the SPA's connections page; the SPA's
 * TanStack Query refetches and the new connection appears.
 */
@RestController
@RequestMapping("/connections/oauth")
@Tag(name = "connections")
public class OAuthCallbackController {

  private final HandleOAuthCallbackUseCase handleCallback;
  private final String spaConnectionsUrl;

  public OAuthCallbackController(
      HandleOAuthCallbackUseCase handleCallback,
      @Value("${soqlexplorer.salesforce.spa-callback-redirect:http://localhost:5173/connections}")
          String spaConnectionsUrl) {
    this.handleCallback = handleCallback;
    this.spaConnectionsUrl = spaConnectionsUrl;
  }

  @GetMapping("/callback")
  @Operation(summary = "Salesforce OAuth post-consent redirect handler")
  public void callback(
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "state", required = false) String state,
      @RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "error_description", required = false) String errorDescription,
      HttpServletResponse response)
      throws IOException {
    if (error != null && !error.isBlank()) {
      response.sendRedirect(redirectWith(error, errorDescription));
      return;
    }
    if (code == null || state == null) {
      response.sendRedirect(redirectWith("oauth_failed", "Missing code or state"));
      return;
    }
    try {
      SalesforceConnection connection = handleCallback.complete(code, state);
      response.sendRedirect(
          spaConnectionsUrl
              + (spaConnectionsUrl.contains("?") ? "&" : "?")
              + "connected="
              + URLEncoder.encode(connection.id().value().toString(), StandardCharsets.UTF_8));
    } catch (SalesforceGatewayException ex) {
      response.sendRedirect(redirectWith("oauth_failed", ex.getMessage()));
    }
  }

  private String redirectWith(String code, String description) {
    StringBuilder sb = new StringBuilder(spaConnectionsUrl);
    sb.append(spaConnectionsUrl.contains("?") ? '&' : '?');
    sb.append("error=").append(URLEncoder.encode(code, StandardCharsets.UTF_8));
    if (description != null && !description.isBlank()) {
      sb.append("&error_description=")
          .append(URLEncoder.encode(description, StandardCharsets.UTF_8));
    }
    return sb.toString();
  }
}
