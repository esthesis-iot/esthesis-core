package esthesis.platform.backend.server.nifi.sinks.readers;

import esthesis.platform.backend.server.nifi.sinks.NiFiSinkFactory;

public interface NiFiReaderFactory extends NiFiSinkFactory {

  /**
   * Returns whether this reader is capable of consuming ping data.
   */
  boolean supportsPingRead();

  /**
   * Returns whether this reader is capable of consuming metadata data.
   */
  boolean supportsMetadataRead();

  /**
   * Returns whether this reader is capable of consuming telemetry data.
   */
  boolean supportsTelemetryRead();

  /**
   * Returns whether this reader is capable of consuming command data;
   * @return
   */
  boolean supportsCommandRead();
}
