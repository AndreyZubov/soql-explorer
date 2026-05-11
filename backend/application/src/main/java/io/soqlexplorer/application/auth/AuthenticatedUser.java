package io.soqlexplorer.application.auth;

import io.soqlexplorer.domain.user.Email;
import io.soqlexplorer.domain.user.Role;
import io.soqlexplorer.domain.user.UserId;
import java.util.Set;

/**
 * Result of a successful authentication. Lean by design — only the bits the web layer needs to
 * mint a JWT. We intentionally do not return the full {@code User} aggregate to keep the public
 * use-case boundary narrow.
 */
public record AuthenticatedUser(UserId id, Email email, Set<Role> roles) {

  public AuthenticatedUser {
    roles = Set.copyOf(roles);
  }
}
