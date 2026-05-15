package io.soqlexplorer.cache;

import static org.assertj.core.api.Assertions.assertThat;

import io.soqlexplorer.domain.connection.ConnectionId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CaffeineSchemaCacheAdapterTest {

  private final CaffeineSchemaCacheAdapter cache =
      new CaffeineSchemaCacheAdapter(new SchemaCacheProperties());

  @Test
  void sObjectNames_only_loads_once_per_connection() {
    ConnectionId id = ConnectionId.of(UUID.randomUUID());
    AtomicInteger calls = new AtomicInteger();

    cache.sObjectNames(id, () -> {
      calls.incrementAndGet();
      return List.of("Account", "Contact");
    });
    cache.sObjectNames(id, () -> {
      calls.incrementAndGet();
      return List.of("Should", "Not", "Be", "Used");
    });

    assertThat(calls.get()).isEqualTo(1);
  }

  @Test
  void describe_caches_per_sobject() {
    ConnectionId id = ConnectionId.of(UUID.randomUUID());
    AtomicInteger calls = new AtomicInteger();

    cache.describe(id, "Account", () -> {
      calls.incrementAndGet();
      return Map.of("name", "Account");
    });
    cache.describe(id, "Account", () -> {
      calls.incrementAndGet();
      return Map.of("name", "Account");
    });
    cache.describe(id, "Contact", () -> {
      calls.incrementAndGet();
      return Map.of("name", "Contact");
    });

    assertThat(calls.get()).isEqualTo(2);
  }

  @Test
  void invalidate_per_connection_clears_only_that_connections_entries() {
    ConnectionId a = ConnectionId.of(UUID.randomUUID());
    ConnectionId b = ConnectionId.of(UUID.randomUUID());
    cache.sObjectNames(a, () -> List.of("A"));
    cache.sObjectNames(b, () -> List.of("B"));
    cache.describe(a, "Account", () -> Map.of("k", "v"));
    cache.describe(b, "Account", () -> Map.of("k", "v"));

    cache.invalidate(a);
    AtomicInteger calls = new AtomicInteger();
    cache.sObjectNames(a, () -> {
      calls.incrementAndGet();
      return List.of();
    });
    cache.sObjectNames(b, () -> {
      calls.incrementAndGet();
      return List.of();
    });
    cache.describe(a, "Account", () -> {
      calls.incrementAndGet();
      return Map.of();
    });
    cache.describe(b, "Account", () -> {
      calls.incrementAndGet();
      return Map.of();
    });

    assertThat(calls.get()).isEqualTo(2);
  }
}
