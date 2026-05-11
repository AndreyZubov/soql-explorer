package io.soqlexplorer.application.ports.clock;

import java.time.Instant;

/**
 * Outbound port for "what time is it" so the application layer can stay free of {@code
 * java.time.Clock} and tests can inject a fixed clock without Spring trickery.
 */
public interface ClockPort {

  Instant now();
}
