package esthesis.platform.server.nifi.sinks.readers;

import esthesis.platform.server.nifi.sinks.NiFiSinkFactory;

public interface NiFiReaderFactory extends NiFiSinkFactory {

  /**
   * Returns whether this reader is capable to consume ping data.
   */
  boolean supportsPingRead();

  /**
   * Returns whether this reader is capable to consume metadata data.
   */
  boolean supportsMetadataRead();

  /**
   * Returns whether this reader is capable to consume telemetry data.
   */
  boolean supportsTelemetryRead();
}
