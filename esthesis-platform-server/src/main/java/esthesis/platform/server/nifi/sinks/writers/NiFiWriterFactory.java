package esthesis.platform.server.nifi.sinks.writers;

import esthesis.platform.server.nifi.sinks.NiFiSinkFactory;

public interface NiFiWriterFactory extends NiFiSinkFactory {

  /**
   * Returns whether writer is capable of writing metadata data.
   */
  boolean supportsMetadataWrite();

  /**
   * Returns whether this writer is capable of writing telemetry data.
   */
  boolean supportsTelemetryWrite();

  /**
   * Returns whether writer is capable of writing ping data.
   */
  boolean supportsPingWrite();

  /**
   * Returns whether writer is capable of writing command data.
   */
  boolean supportsCommandWrite();

}
