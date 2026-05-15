package io.soqlexplorer.application.ports.salesforce;

/**
 * Snapshot of Salesforce REST API consumption, parsed from the {@code Sforce-Limit-Info} response
 * header.
 *
 * <p>Salesforce returns this header on every REST response. The adapter parses the
 * {@code api-usage=N/M} pair and surfaces it through this value object so the application layer
 * can hand it to the UI without exposing HTTP details.
 */
public record ApiUsageInfo(int used, int limit) {

  public ApiUsageInfo {
    if (used < 0) {
      throw new IllegalArgumentException("used must be >= 0");
    }
    if (limit <= 0) {
      throw new IllegalArgumentException("limit must be > 0");
    }
  }

  public int percentUsed() {
    return (int) Math.round((used * 100.0) / limit);
  }
}
