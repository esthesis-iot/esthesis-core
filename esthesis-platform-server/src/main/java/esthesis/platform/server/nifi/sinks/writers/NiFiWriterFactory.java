package esthesis.platform.server.nifi.sinks.writers;

import esthesis.platform.server.nifi.sinks.NiFiSinkFactory;

public interface NiFiWriterFactory extends NiFiSinkFactory {

  /**
   * Returns whether writer is capable to read metadata data.
   */
  boolean supportsMetadataWrite();

  /**
   * Returns whether this writer is capable to read telemetry data.
   */
  boolean supportsTelemetryWrite();

  /**
   * Returns whether writer is capable to read ping data.
   */
  boolean supportsPingWrite();

}
