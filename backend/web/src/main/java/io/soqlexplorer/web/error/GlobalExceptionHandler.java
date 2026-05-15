package io.soqlexplorer.web.error;

import io.soqlexplorer.application.auth.InvalidCredentialsException;
import io.soqlexplorer.application.connection.ConnectionNotFoundException;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayException;
import io.soqlexplorer.web.security.InvalidJwtException;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain/application exceptions to RFC 7807 {@link ProblemDetail} responses.
 *
 * <p>Every {@code type} URI is rooted at {@code https://soql-explorer.io/errors/...} so the SPA
 * (and any future API consumers) get a stable, dereferenceable identifier per error class.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String TYPE_BASE = "https://soql-explorer.io/errors/";

  @ExceptionHandler(InvalidCredentialsException.class)
  public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
    return problem(HttpStatus.UNAUTHORIZED, "invalid-credentials", ex.getMessage());
  }

  @ExceptionHandler(InvalidJwtException.class)
  public ProblemDetail handleInvalidJwt(InvalidJwtException ex) {
    return problem(HttpStatus.UNAUTHORIZED, "invalid-jwt", ex.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
    return problem(HttpStatus.FORBIDDEN, "access-denied", "Access denied");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
    ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "validation-failed", "Request validation failed");
    pd.setProperty("violations", details);
    return pd;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleBadInput(IllegalArgumentException ex) {
    return problem(HttpStatus.BAD_REQUEST, "bad-request", ex.getMessage());
  }

  @ExceptionHandler(ConnectionNotFoundException.class)
  public ProblemDetail handleConnectionNotFound(ConnectionNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "connection-not-found", ex.getMessage());
  }

  @ExceptionHandler(SalesforceGatewayException.class)
  public ProblemDetail handleSalesforce(SalesforceGatewayException ex) {
    HttpStatus status =
        switch (ex.kind()) {
          case AUTH -> HttpStatus.UNAUTHORIZED;
          case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
          case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
          case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
          case CIRCUIT_OPEN, UPSTREAM_5XX -> HttpStatus.BAD_GATEWAY;
        };
    return problem(status, "salesforce-" + ex.kind().name().toLowerCase().replace('_', '-'), ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex) {
    // Deliberately generic message — never echo internal exception details to clients.
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "An unexpected error occurred");
  }

  private static ProblemDetail problem(HttpStatus status, String slug, String detail) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setType(URI.create(TYPE_BASE + slug));
    pd.setTitle(slug);
    return pd;
  }
}
