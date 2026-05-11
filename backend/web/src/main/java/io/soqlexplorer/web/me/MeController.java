package io.soqlexplorer.web.me;

import io.soqlexplorer.domain.user.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Trivial protected endpoint used to satisfy the Step 1 exit criterion: "log in from the SPA,
 * hit a protected stub endpoint with the JWT". Later steps replace this with the real user/me
 * payload (favorite connection, recent history summary, etc.).
 */
@RestController
@RequestMapping("/me")
@Tag(name = "me", description = "Current authenticated user")
public class MeController {

  @GetMapping
  @Operation(summary = "Return the authenticated principal")
  public MeResponse me(@AuthenticationPrincipal UserId principal, Authentication authentication) {
    List<String> roles =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
            .toList();
    return new MeResponse(principal.value(), roles);
  }

  public record MeResponse(UUID userId, List<String> roles) {}
}
