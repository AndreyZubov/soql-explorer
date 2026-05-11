package io.soqlexplorer.application.ports.salesforce;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * One page of SOQL results.
 *
 * <p>{@code records} is intentionally typed as {@code List<Map<String, Object>>} — Salesforce
 * returns dynamic, schema-defined documents and we shouldn't impose a closed shape at this
 * boundary. {@code nextRecordsUrl} is empty when the result set is exhausted.
 */
public record QueryPage(
    List<Map<String, Object>> records, int totalSize, boolean done, Optional<String> nextRecordsUrl) {

  public QueryPage {
    Objects.requireNonNull(records, "records");
    Objects.requireNonNull(nextRecordsUrl, "nextRecordsUrl");
    if (totalSize < 0) {
      throw new IllegalArgumentException("totalSize must be >= 0");
    }
    records = List.copyOf(records);
  }
}
