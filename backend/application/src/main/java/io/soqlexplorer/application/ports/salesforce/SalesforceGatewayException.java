package io.soqlexplorer.application.ports.salesforce;

/**
 * Generic checked-style failure surfaced from the Salesforce adapter.
 *
 * <p>Unchecked so use-case methods don't pollute their signatures, but distinct from
 * {@link RuntimeException} so the web layer's {@code @ControllerAdvice} can map it to a 502 / 504
 * with a proper RFC 7807 body.
 */
public class SalesforceGatewayException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final Kind kind;

  public SalesforceGatewayException(Kind kind, String message) {
    super(message);
    this.kind = kind;
  }

  public SalesforceGatewayException(Kind kind, String message, Throwable cause) {
    super(message, cause);
    this.kind = kind;
  }

  public Kind kind() {
    return kind;
  }

  public enum Kind {
    AUTH,
    RATE_LIMITED,
    BAD_REQUEST,
    UPSTREAM_5XX,
    CIRCUIT_OPEN,
    TIMEOUT
  }
}
