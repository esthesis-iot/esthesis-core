package esthesis.platform.server.nifi.sinks.producers;

import esthesis.platform.server.nifi.sinks.NiFiSinkFactory;

public interface NiFiProducerFactory extends NiFiSinkFactory {

  /**
   * Returns whether this producer is capable to produce telemetry data.
   */
  boolean supportsTelemetryProduce();

  /**
   * Returns whether this producer is capable to produce metadata data.
   */
  boolean supportsMetadataProduce();
}
