package io.soqlexplorer.application.auth;

/**
 * Thrown by the authentication use case when the supplied credentials are wrong or the user is
 * disabled. The message is intentionally generic — the web layer must not leak whether it was the
 * email or the password that didn't match.
 */
public class InvalidCredentialsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidCredentialsException() {
    super("Invalid credentials");
  }
}
