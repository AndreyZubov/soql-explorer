package io.soqlexplorer.application.ports.schema;

import io.soqlexplorer.domain.connection.ConnectionId;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Outbound port for the per-connection schema cache (sObject lists + describe payloads).
 *
 * <p>The use cases drive cache hits/misses through this port rather than the cache library
 * directly, which keeps Caffeine out of the application module's dependency graph. Each method
 * accepts a {@link Supplier} that loads from Salesforce on miss; the adapter caches the result.
 *
 * <p>Cache invalidation is per-connection (so deleting a connection clears its slice) and
 * global ({@link #invalidateAll()} for the admin-level {@code DELETE /api/v1/cache/schema}).
 */
public interface SchemaCachePort {

  List<String> sObjectNames(ConnectionId connectionId, Supplier<List<String>> loader);

  Map<String, Object> describe(
      ConnectionId connectionId, String sObjectName, Supplier<Map<String, Object>> loader);

  void invalidate(ConnectionId connectionId);

  void invalidateAll();
}
