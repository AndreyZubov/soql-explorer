package io.soqlexplorer.web.connections;

/**
 * Outbound payload for {@code POST /connections/oauth/start}.
 *
 * <p>The SPA navigates {@code window.location.href = authorizationUrl} to start the flow. The
 * state is also returned so the SPA can store it (e.g. in {@code sessionStorage}) and verify on
 * the post-callback bounce-back if desired.
 */
public record StartOAuthResponse(String authorizationUrl, String state) {}
