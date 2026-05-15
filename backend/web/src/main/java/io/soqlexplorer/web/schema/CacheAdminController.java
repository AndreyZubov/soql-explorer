package io.soqlexplorer.web.schema;

import io.soqlexplorer.application.schema.InvalidateSchemaCacheUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only operations on the schema cache.
 *
 * <p>The plan mentions {@code DELETE /api/v1/cache/schema}; we mount it at {@code
 * /api/v1/cache/schema} so it can be proxied by the SPA prefix and is visible separately from
 * regular API surface.
 */
@RestController
@RequestMapping("/api/v1/cache")
@Tag(name = "cache", description = "Schema cache administration")
public class CacheAdminController {

  private final InvalidateSchemaCacheUseCase invalidate;

  public CacheAdminController(InvalidateSchemaCacheUseCase invalidate) {
    this.invalidate = invalidate;
  }

  @DeleteMapping("/schema")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Drop the entire schema cache (admin-only)")
  public ResponseEntity<Void> invalidateSchemaCache() {
    invalidate.invalidateAll();
    return ResponseEntity.noContent().build();
  }
}
