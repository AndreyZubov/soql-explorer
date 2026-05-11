package io.soqlexplorer.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.soqlexplorer.application.auth.AuthenticatedUser;
import io.soqlexplorer.application.ports.clock.ClockPort;
import io.soqlexplorer.domain.user.Email;
import io.soqlexplorer.domain.user.Role;
import io.soqlexplorer.domain.user.UserId;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Issues and validates HS256 JWTs.
 *
 * <p>The token carries:
 *
 * <ul>
 *   <li>{@code sub}: user id (UUID),
 *   <li>{@code email}: user email,
 *   <li>{@code roles}: list of {@link Role} names,
 *   <li>{@code typ}: {@code access} or {@code refresh}.
 * </ul>
 *
 * <p>Refresh tokens are delivered to the SPA via an HttpOnly, Secure, SameSite=Strict cookie;
 * access tokens travel in the {@code Authorization: Bearer} header.
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

  public static final String ACCESS_TYPE = "access";
  public static final String REFRESH_TYPE = "refresh";

  private final JwtProperties props;
  private final ClockPort clock;
  private final SecretKey signingKey;

  public JwtService(JwtProperties props, ClockPort clock) {
    this.props = props;
    this.clock = clock;
    byte[] keyBytes = decodeKey(props.getSecret());
    if (keyBytes.length < 32) {
      throw new IllegalStateException(
          "JWT signing key must be at least 256 bits (32 bytes) of entropy");
    }
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
  }

  public IssuedToken issueAccessToken(AuthenticatedUser user) {
    return issue(user, ACCESS_TYPE, Duration.ofSeconds(props.getAccessTokenTtlSeconds()));
  }

  public IssuedToken issueRefreshToken(AuthenticatedUser user) {
    return issue(user, REFRESH_TYPE, Duration.ofSeconds(props.getRefreshTokenTtlSeconds()));
  }

  /** Parses and verifies the supplied JWT, asserting the expected {@code typ} claim. */
  public ParsedToken parse(String jwt, String expectedType) {
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(signingKey)
              .requireIssuer(props.getIssuer())
              .build()
              .parseSignedClaims(jwt)
              .getPayload();
      String type = claims.get("typ", String.class);
      if (!expectedType.equals(type)) {
        throw new JwtException("Unexpected token type: " + type);
      }
      Set<Role> roles =
          claims.get("roles", List.class) == null
              ? EnumSet.of(Role.USER)
              : ((List<?>) claims.get("roles"))
                  .stream()
                      .map(Object::toString)
                      .map(Role::valueOf)
                      .reduce(
                          EnumSet.noneOf(Role.class),
                          (acc, r) -> {
                            acc.add(r);
                            return acc;
                          },
                          (a, b) -> {
                            a.addAll(b);
                            return a;
                          });
      return new ParsedToken(
          UserId.of(UUID.fromString(claims.getSubject())),
          Email.of(claims.get("email", String.class)),
          roles,
          claims.getExpiration().toInstant());
    } catch (JwtException | IllegalArgumentException ex) {
      throw new InvalidJwtException("Invalid JWT", ex);
    }
  }

  private IssuedToken issue(AuthenticatedUser user, String type, Duration ttl) {
    Instant now = clock.now();
    Instant expiresAt = now.plus(ttl);
    String jwt =
        Jwts.builder()
            .issuer(props.getIssuer())
            .subject(user.id().value().toString())
            .claim("email", user.email().value())
            .claim("roles", user.roles().stream().map(Enum::name).toList())
            .claim("typ", type)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    return new IssuedToken(jwt, expiresAt);
  }

  private static byte[] decodeKey(String secret) {
    try {
      return Base64.getDecoder().decode(secret);
    } catch (IllegalArgumentException notBase64) {
      // Allow raw-string secrets in local dev — but the entropy check still applies.
      return secret.getBytes(StandardCharsets.UTF_8);
    }
  }

  public record IssuedToken(String token, Instant expiresAt) {}

  public record ParsedToken(UserId userId, Email email, Set<Role> roles, Instant expiresAt) {}
}
