package io.soqlexplorer.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that maps a Bearer JWT to a Spring Security {@code Authentication}.
 *
 * <p>The filter is deliberately permissive on absent tokens — the security configuration is the
 * one that denies access to protected routes when no authentication is present. This keeps
 * preflight (OPTIONS) and public endpoints clean.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      try {
        JwtService.ParsedToken parsed =
            jwtService.parse(header.substring(BEARER_PREFIX.length()), JwtService.ACCESS_TYPE);
        List<SimpleGrantedAuthority> authorities =
            parsed.roles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.name())).toList();
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(parsed.userId(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (InvalidJwtException ignored) {
        // Leave context empty — security config will reject protected routes with 401.
        SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(request, response);
  }
}
