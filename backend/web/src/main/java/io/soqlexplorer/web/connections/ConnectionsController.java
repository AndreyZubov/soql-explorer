package io.soqlexplorer.web.connections;

import io.soqlexplorer.application.connection.DeleteConnectionUseCase;
import io.soqlexplorer.application.connection.ListConnectionsUseCase;
import io.soqlexplorer.application.connection.SetDefaultConnectionUseCase;
import io.soqlexplorer.application.connection.StartOAuthFlowUseCase;
import io.soqlexplorer.application.ports.salesforce.ApiUsagePort;
import io.soqlexplorer.application.ports.salesforce.OAuthAuthorizationRequest;
import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.connection.Environment;
import io.soqlexplorer.domain.user.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated CRUD over the user's Salesforce connections.
 *
 * <p>OAuth callback handling lives in {@link OAuthCallbackController} because the callback is
 * reached by the browser's SF redirect without an access token in the {@code Authorization}
 * header — it must be reachable from a {@code permitAll} security path.
 */
@RestController
@RequestMapping("/connections")
@Tag(name = "connections", description = "Salesforce connection management")
public class ConnectionsController {

  private final ListConnectionsUseCase listConnections;
  private final StartOAuthFlowUseCase startOAuthFlow;
  private final SetDefaultConnectionUseCase setDefault;
  private final DeleteConnectionUseCase deleteConnection;
  private final ApiUsagePort apiUsage;

  public ConnectionsController(
      ListConnectionsUseCase listConnections,
      StartOAuthFlowUseCase startOAuthFlow,
      SetDefaultConnectionUseCase setDefault,
      DeleteConnectionUseCase deleteConnection,
      ApiUsagePort apiUsage) {
    this.listConnections = listConnections;
    this.startOAuthFlow = startOAuthFlow;
    this.setDefault = setDefault;
    this.deleteConnection = deleteConnection;
    this.apiUsage = apiUsage;
  }

  @GetMapping
  @Operation(summary = "List the caller's Salesforce connections")
  public List<ConnectionDto> list(@AuthenticationPrincipal UserId principal) {
    return listConnections.list(principal).stream().map(ConnectionDto::from).toList();
  }

  @PostMapping("/oauth/start")
  @Operation(summary = "Begin a Salesforce OAuth Web Server Flow")
  public StartOAuthResponse startOAuth(
      @AuthenticationPrincipal UserId principal, @Valid @RequestBody StartOAuthRequest body) {
    OAuthAuthorizationRequest auth =
        startOAuthFlow.start(principal, Environment.valueOf(body.environment()));
    return new StartOAuthResponse(auth.authorizationUrl().toString(), auth.state());
  }

  @PutMapping("/{id}/default")
  @Operation(summary = "Mark a connection as the caller's default")
  public ConnectionDto markDefault(
      @AuthenticationPrincipal UserId principal, @PathVariable("id") UUID id) {
    return ConnectionDto.from(setDefault.setDefault(principal, ConnectionId.of(id)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a connection")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal UserId principal, @PathVariable("id") UUID id) {
    deleteConnection.delete(principal, ConnectionId.of(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/api-usage")
  @Operation(summary = "Most recently observed Salesforce API usage for the connection")
  public ResponseEntity<ApiUsageDto> apiUsageFor(
      @AuthenticationPrincipal UserId principal, @PathVariable("id") UUID id) {
    // Authorize: the caller must own the connection. We don't need the connection itself, just
    // the ownership check — reuse list-connections to keep the surface area minimal.
    boolean owns =
        listConnections.list(principal).stream().anyMatch(c -> c.id().value().equals(id));
    if (!owns) {
      return ResponseEntity.notFound().build();
    }
    return apiUsage
        .latestFor(ConnectionId.of(id))
        .map(info -> ResponseEntity.ok(ApiUsageDto.from(info)))
        .orElseGet(() -> ResponseEntity.noContent().build());
  }
}
