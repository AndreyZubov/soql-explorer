package io.soqlexplorer.application.schema;

import io.soqlexplorer.application.ports.schema.SchemaCachePort;
import java.util.Objects;

/** Default implementation of {@link InvalidateSchemaCacheUseCase}. */
public class InvalidateSchemaCacheService implements InvalidateSchemaCacheUseCase {

  private final SchemaCachePort cache;

  public InvalidateSchemaCacheService(SchemaCachePort cache) {
    this.cache = Objects.requireNonNull(cache, "cache");
  }

  @Override
  public void invalidateAll() {
    cache.invalidateAll();
  }
}
