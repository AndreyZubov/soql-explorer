package io.soqlexplorer.web.security;

/** Thrown when a JWT fails signature, claim, or type validation. Mapped to HTTP 401. */
public class InvalidJwtException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidJwtException(String message, Throwable cause) {
    super(message, cause);
  }
}
