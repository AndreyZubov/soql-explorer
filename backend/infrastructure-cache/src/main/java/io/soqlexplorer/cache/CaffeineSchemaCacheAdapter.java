package io.soqlexplorer.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.soqlexplorer.application.ports.schema.SchemaCachePort;
import io.soqlexplorer.domain.connection.ConnectionId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * Caffeine-backed implementation of {@link SchemaCachePort}.
 *
 * <p>Two underlying caches:
 *
 * <ul>
 *   <li>{@code sobjectsCache} keyed by {@link ConnectionId} — short TTL because admins drop
 *       fields frequently during dev work.
 *   <li>{@code describeCache} keyed by a composite ({@link ConnectionId}, sObject name) — longer
 *       TTL but a hard global size cap to avoid unbounded memory growth.
 * </ul>
 */
@Component
public class CaffeineSchemaCacheAdapter implements SchemaCachePort {

  private final Cache<ConnectionId, List<String>> sobjectsCache;
  private final Cache<DescribeKey, Map<String, Object>> describeCache;

  public CaffeineSchemaCacheAdapter(SchemaCacheProperties props) {
    Objects.requireNonNull(props, "props");
    this.sobjectsCache =
        Caffeine.newBuilder()
            .expireAfterWrite(props.getSobjectsTtl())
            .maximumSize(props.getSobjectsMaxSize())
            .build();
    this.describeCache =
        Caffeine.newBuilder()
            .expireAfterWrite(props.getDescribeTtl())
            .maximumSize(props.getDescribeMaxSize())
            .build();
  }

  @Override
  public List<String> sObjectNames(ConnectionId connectionId, Supplier<List<String>> loader) {
    Objects.requireNonNull(connectionId, "connectionId");
    Objects.requireNonNull(loader, "loader");
    return sobjectsCache.get(connectionId, key -> List.copyOf(loader.get()));
  }

  @Override
  public Map<String, Object> describe(
      ConnectionId connectionId, String sObjectName, Supplier<Map<String, Object>> loader) {
    Objects.requireNonNull(connectionId, "connectionId");
    Objects.requireNonNull(sObjectName, "sObjectName");
    Objects.requireNonNull(loader, "loader");
    return describeCache.get(new DescribeKey(connectionId, sObjectName), key -> loader.get());
  }

  @Override
  public void invalidate(ConnectionId connectionId) {
    Objects.requireNonNull(connectionId, "connectionId");
    sobjectsCache.invalidate(connectionId);
    describeCache.asMap().keySet().removeIf(key -> key.connectionId().equals(connectionId));
  }

  @Override
  public void invalidateAll() {
    sobjectsCache.invalidateAll();
    describeCache.invalidateAll();
  }

  private record DescribeKey(ConnectionId connectionId, String sObjectName) {}
}
