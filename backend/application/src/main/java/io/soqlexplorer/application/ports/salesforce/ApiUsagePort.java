package io.soqlexplorer.application.ports.salesforce;

import io.soqlexplorer.domain.connection.ConnectionId;
import java.util.Optional;

/**
 * Outbound port exposing the most recently observed Salesforce API consumption for a connection.
 *
 * <p>The adapter parses the {@code Sforce-Limit-Info} response header on every REST call and
 * keeps the latest value per connection. The web layer reads it through this port so the SPA can
 * show an {@code <ApiUsageBadge>} without issuing a dedicated probe call to Salesforce.
 */
public interface ApiUsagePort {

  /** Returns the latest usage snapshot for the connection, if one has been observed. */
  Optional<ApiUsageInfo> latestFor(ConnectionId connectionId);
}
