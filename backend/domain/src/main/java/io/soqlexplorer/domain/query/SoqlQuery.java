package io.soqlexplorer.domain.query;

import java.util.Locale;
import java.util.Objects;

/**
 * Value object wrapping a SOQL string with the basic, framework-free invariants enforced.
 *
 * <p>Two checks are performed here:
 *
 * <ul>
 *   <li>Length cap of 20 000 characters (matches Salesforce API limits and the SPEC).
 *   <li>"Read-only" guard: a small allow-list rejects clearly DML/DDL statements at construction
 *       time. A full ANTLR-based validator lives in the application layer (Step 3) — this is a
 *       belt-and-braces guard so a bad string can't propagate even if the validator is bypassed.
 * </ul>
 */
public record SoqlQuery(String text) {

  public static final int MAX_LENGTH = 20_000;

  public SoqlQuery {
    Objects.requireNonNull(text, "SOQL text must not be null");
    String trimmed = text.strip();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("SOQL text must not be blank");
    }
    if (trimmed.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "SOQL text exceeds maximum length of " + MAX_LENGTH + " characters");
    }
    String head = trimmed.toUpperCase(Locale.ROOT);
    if (!head.startsWith("SELECT") && !head.startsWith("FIND")) {
      throw new IllegalArgumentException("SOQL must begin with SELECT or FIND");
    }
    if (containsForbiddenKeyword(head)) {
      throw new IllegalArgumentException("DML/DDL statements are not allowed");
    }
  }

  public static SoqlQuery of(String text) {
    return new SoqlQuery(text);
  }

  private static boolean containsForbiddenKeyword(String upper) {
    // Tokenized check to avoid false positives like INSERT_DATE in a field name.
    for (String forbidden : new String[] {"INSERT ", "UPDATE ", "DELETE ", "UPSERT ", "MERGE "}) {
      if (upper.contains(forbidden)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return text;
  }
}
