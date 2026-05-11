package io.soqlexplorer.web.auth;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Successful login payload.
 *
 * <p>The access token is delivered in the body so the SPA can attach it to the {@code
 * Authorization} header. The refresh token is set as an HttpOnly cookie and is NOT included in
 * this payload — see {@code AuthController#login}.
 */
public record LoginResponse(
    String accessToken, Instant accessTokenExpiresAt, UUID userId, String email, Set<String> roles) {}
