package io.soqlexplorer.domain.connection;

/** Salesforce environment kind — drives the OAuth login URL used during authorization. */
public enum Environment {
  PRODUCTION("https://login.salesforce.com"),
  SANDBOX("https://test.salesforce.com");

  private final String loginHost;

  Environment(String loginHost) {
    this.loginHost = loginHost;
  }

  public String loginHost() {
    return loginHost;
  }
}
