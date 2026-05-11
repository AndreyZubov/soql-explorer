package io.soqlexplorer.web.auth;

import io.soqlexplorer.application.auth.AuthenticateUserUseCase;
import io.soqlexplorer.application.auth.AuthenticatedUser;
import io.soqlexplorer.application.auth.InvalidCredentialsException;
import io.soqlexplorer.domain.user.Email;
import io.soqlexplorer.web.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Local-credential authentication endpoints.
 *
 * <ul>
 *   <li>{@code POST /auth/login} — exchange email + password for an access JWT + refresh cookie.
 *   <li>{@code POST /auth/refresh} — exchange a refresh cookie for a new access JWT.
 *   <li>{@code POST /auth/logout} — clear the refresh cookie.
 * </ul>
 *
 * <p>The refresh token never appears in a response body to prevent JS access from the SPA; it
 * lives in an HttpOnly, Secure, SameSite=Strict cookie.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "auth", description = "Local-credential authentication")
public class AuthController {

  private static final String REFRESH_COOKIE = "refresh_token";

  private final AuthenticateUserUseCase authenticate;
  private final JwtService jwt;

  public AuthController(AuthenticateUserUseCase authenticate, JwtService jwt) {
    this.authenticate = authenticate;
    this.jwt = jwt;
  }

  @PostMapping("/login")
  @Operation(summary = "Authenticate with local credentials")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest body, HttpServletResponse response) {
    AuthenticatedUser user;
    try {
      user = authenticate.authenticate(Email.of(body.email()), body.password());
    } catch (InvalidCredentialsException ex) {
      // Re-throw — global @ControllerAdvice maps to RFC 7807 401.
      throw ex;
    }
    JwtService.IssuedToken access = jwt.issueAccessToken(user);
    JwtService.IssuedToken refresh = jwt.issueRefreshToken(user);
    response.addCookie(buildRefreshCookie(refresh.token(), refreshTtlSeconds(refresh)));
    return ResponseEntity.ok(
        new LoginResponse(
            access.token(),
            access.expiresAt(),
            user.id().value(),
            user.email().value(),
            user.roles().stream().map(Enum::name).collect(Collectors.toSet())));
  }

  @PostMapping("/refresh")
  @Operation(summary = "Exchange the refresh cookie for a new access token")
  public ResponseEntity<LoginResponse> refresh(
      HttpServletRequest request, HttpServletResponse response) {
    Cookie cookie = readCookie(request);
    if (cookie == null) {
      throw new InvalidCredentialsException();
    }
    JwtService.ParsedToken parsed = jwt.parse(cookie.getValue(), JwtService.REFRESH_TYPE);
    AuthenticatedUser user = new AuthenticatedUser(parsed.userId(), parsed.email(), parsed.roles());
    JwtService.IssuedToken access = jwt.issueAccessToken(user);
    // Rotate the refresh token on each use to limit replay if it ever leaks.
    JwtService.IssuedToken rotatedRefresh = jwt.issueRefreshToken(user);
    response.addCookie(buildRefreshCookie(rotatedRefresh.token(), refreshTtlSeconds(rotatedRefresh)));
    return ResponseEntity.ok(
        new LoginResponse(
            access.token(),
            access.expiresAt(),
            user.id().value(),
            user.email().value(),
            user.roles().stream().map(Enum::name).collect(Collectors.toSet())));
  }

  @PostMapping("/logout")
  @Operation(summary = "Clear the refresh cookie")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    Cookie clear = buildRefreshCookie("", 0);
    response.addCookie(clear);
    return ResponseEntity.noContent().build();
  }

  private static Cookie readCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }
    for (Cookie c : request.getCookies()) {
      if (REFRESH_COOKIE.equals(c.getName())) {
        return c;
      }
    }
    return null;
  }

  private static Cookie buildRefreshCookie(String value, int maxAgeSeconds) {
    Cookie cookie = new Cookie(REFRESH_COOKIE, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(maxAgeSeconds);
    // SameSite is set via header below — the Servlet API still ships without first-class support.
    cookie.setAttribute("SameSite", "Strict");
    return cookie;
  }

  // Seconds until the cookie should expire — derived from the token's own expiry so the cookie
  // and the JWT can never get out of sync.
  private static int refreshTtlSeconds(JwtService.IssuedToken refresh) {
    long seconds = java.time.Duration.between(java.time.Instant.now(), refresh.expiresAt()).toSeconds();
    return (int) Math.max(0L, Math.min(seconds, Integer.MAX_VALUE));
  }
}
