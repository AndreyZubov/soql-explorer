package io.soqlexplorer.domain.connection;

import java.util.Objects;

/**
 * Salesforce organization identifier (the 15- or 18-character Org ID).
 *
 * <p>We accept both lengths and never attempt to convert between them — the 15-character ID is
 * case-sensitive, the 18-character ID is case-insensitive. Callers must use whichever Salesforce
 * itself returned for that org.
 */
public record OrgId(String value) {

  public OrgId {
    Objects.requireNonNull(value, "OrgId value must not be null");
    if (value.length() != 15 && value.length() != 18) {
      throw new IllegalArgumentException(
          "Salesforce Org ID must be 15 or 18 characters long, got " + value.length());
    }
  }

  public static OrgId of(String value) {
    return new OrgId(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
