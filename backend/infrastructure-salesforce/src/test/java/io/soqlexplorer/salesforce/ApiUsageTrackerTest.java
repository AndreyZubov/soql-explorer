package io.soqlexplorer.salesforce;

import static org.assertj.core.api.Assertions.assertThat;

import io.soqlexplorer.domain.connection.ConnectionId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ApiUsageTrackerTest {

  @Test
  void parses_well_formed_header() {
    ApiUsageTracker tracker = new ApiUsageTracker();
    ConnectionId id = ConnectionId.of(UUID.randomUUID());

    tracker.record(id, "api-usage=42/15000");

    assertThat(tracker.latestFor(id)).isPresent();
    assertThat(tracker.latestFor(id).orElseThrow().used()).isEqualTo(42);
    assertThat(tracker.latestFor(id).orElseThrow().limit()).isEqualTo(15000);
    assertThat(tracker.latestFor(id).orElseThrow().percentUsed()).isZero();
  }

  @Test
  void ignores_malformed_header() {
    ApiUsageTracker tracker = new ApiUsageTracker();
    ConnectionId id = ConnectionId.of(UUID.randomUUID());

    tracker.record(id, "garbage");

    assertThat(tracker.latestFor(id)).isEmpty();
  }

  @Test
  void keeps_per_connection_records() {
    ApiUsageTracker tracker = new ApiUsageTracker();
    ConnectionId a = ConnectionId.of(UUID.randomUUID());
    ConnectionId b = ConnectionId.of(UUID.randomUUID());

    tracker.record(a, "api-usage=1/1000");
    tracker.record(b, "api-usage=999/1000");

    assertThat(tracker.latestFor(a).orElseThrow().used()).isEqualTo(1);
    assertThat(tracker.latestFor(b).orElseThrow().used()).isEqualTo(999);
  }
}
