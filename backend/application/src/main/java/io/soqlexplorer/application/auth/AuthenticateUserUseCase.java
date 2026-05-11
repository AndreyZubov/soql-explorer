package io.soqlexplorer.application.auth;

import io.soqlexplorer.domain.user.Email;

/**
 * Inbound use case for local-credential authentication.
 *
 * <p>Returns the authenticated {@link AuthenticatedUser} or throws
 * {@link InvalidCredentialsException}. JWT issuance lives in the web layer — this use case stays
 * neutral of the token format.
 */
public interface AuthenticateUserUseCase {

  AuthenticatedUser authenticate(Email email, String rawPassword);
}
