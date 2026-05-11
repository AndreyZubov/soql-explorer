package io.soqlexplorer.domain.user;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a syntactically-valid email address.
 *
 * <p>Validation here is intentionally pragmatic (RFC 5322 is famously hostile to regex). The
 * single source of truth for "is this a real, deliverable email" is the IDP / mail server; this
 * type only guards against obviously malformed input crossing layer boundaries.
 */
public record Email(String value) {

  // Pragmatic RFC 5322 subset: local-part @ domain with at least one dot.
  private static final Pattern PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  public Email {
    Objects.requireNonNull(value, "Email value must not be null");
    if (value.length() > 254) {
      throw new IllegalArgumentException("Email exceeds RFC 5321 length cap of 254 characters");
    }
    if (!PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Email has invalid syntax");
    }
  }

  public static Email of(String value) {
    return new Email(value.toLowerCase());
  }

  @Override
  public String toString() {
    return value;
  }
}
