package esthesis.platform.server.nifi.sinks.producers;

import esthesis.platform.server.nifi.sinks.NiFiSinkFactory;

public interface NiFiProducerFactory extends NiFiSinkFactory {

  /**
   * Returns whether this producer is capable to read metadata data.
   */
  boolean supportsMetadataProduce();

  /**
   * Returns whether this producer is capable to read telemetry data.
   */
  boolean supportsTelemetryProduce();
}
