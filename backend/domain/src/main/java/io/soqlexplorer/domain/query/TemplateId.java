package io.soqlexplorer.domain.query;

import java.util.Objects;
import java.util.UUID;

/** Strongly-typed identifier for a {@link QueryTemplate}. */
public record TemplateId(UUID value) {

  public TemplateId {
    Objects.requireNonNull(value, "TemplateId value must not be null");
  }

  public static TemplateId newId() {
    return new TemplateId(UUID.randomUUID());
  }

  public static TemplateId of(UUID value) {
    return new TemplateId(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
