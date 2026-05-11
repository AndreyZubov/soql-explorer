package io.soqlexplorer.persistence.clock;

import io.soqlexplorer.application.ports.clock.ClockPort;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Wall-clock implementation of {@link ClockPort}.
 *
 * <p>Defined in the persistence module purely because it's the lowest layer that already depends
 * on Spring. Tests substitute it with a deterministic stub.
 */
@Component
public class SystemClockAdapter implements ClockPort {

  private final Clock clock = Clock.systemUTC();

  @Override
  public Instant now() {
    return clock.instant();
  }
}
