package io.soqlexplorer.application.schema;

/** Inbound use case for clearing the entire schema cache — admin-only. */
public interface InvalidateSchemaCacheUseCase {

  void invalidateAll();
}
