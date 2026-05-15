package io.soqlexplorer.application.connection;

/** Thrown when a connection is not visible to the requesting user — either missing or not owned. */
public class ConnectionNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ConnectionNotFoundException(String message) {
    super(message);
  }
}
