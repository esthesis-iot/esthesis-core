package esthesis.platform.server.nifi.sinks.writers;

import esthesis.platform.server.nifi.sinks.NiFiSinkFactory;

public interface NiFiWriterFactory extends NiFiSinkFactory {

  /**
   * Returns whether writer is capable to write metadata data.
   */
  boolean supportsMetadataWrite();

  /**
   * Returns whether this writer is capable to write telemetry data.
   */
  boolean supportsTelemetryWrite();

  /**
   * Returns whether writer is capable to write ping data.
   */
  boolean supportsPingWrite();

}
