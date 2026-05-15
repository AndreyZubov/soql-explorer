package io.soqlexplorer.salesforce;

import io.soqlexplorer.application.ports.salesforce.ApiUsageInfo;
import io.soqlexplorer.application.ports.salesforce.ApiUsagePort;
import io.soqlexplorer.domain.connection.ConnectionId;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Parses the {@code Sforce-Limit-Info} response header (format: {@code api-usage=N/M}) and stores
 * the latest value per connection.
 *
 * <p>The same component implements {@link ApiUsagePort} (read side) and exposes a package-private
 * {@code record(connection, headerValue)} (write side) used by the WebClient adapter.
 */
@Component
public class ApiUsageTracker implements ApiUsagePort {

  private static final Pattern API_USAGE_PATTERN =
      Pattern.compile("api-usage=(\\d+)/(\\d+)", Pattern.CASE_INSENSITIVE);

  private final ConcurrentMap<ConnectionId, ApiUsageInfo> latest = new ConcurrentHashMap<>();

  @Override
  public Optional<ApiUsageInfo> latestFor(ConnectionId connectionId) {
    return Optional.ofNullable(latest.get(connectionId));
  }

  void record(ConnectionId connectionId, String headerValue) {
    if (headerValue == null) {
      return;
    }
    Matcher matcher = API_USAGE_PATTERN.matcher(headerValue);
    if (matcher.find()) {
      try {
        int used = Integer.parseInt(matcher.group(1));
        int limit = Integer.parseInt(matcher.group(2));
        latest.put(connectionId, new ApiUsageInfo(used, limit));
      } catch (IllegalArgumentException ignored) {
        // Malformed value — silently ignore; the badge will just keep showing the previous one.
      }
    }
  }

  void forget(ConnectionId connectionId) {
    latest.remove(connectionId);
  }
}
