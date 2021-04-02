package esthesis.platform.backend.server.nifi.sinks.producers;

import esthesis.platform.backend.server.nifi.sinks.NiFiSinkFactory;

public interface NiFiProducerFactory extends NiFiSinkFactory {

  /**
   * Returns whether this producer is capable of producing telemetry data.
   */
  boolean supportsTelemetryProduce();

  /**
   * Returns whether this producer is capable of producing metadata data.
   */
  boolean supportsMetadataProduce();

  /**
   * Returns whether this producer is capable of producing command data.
   */
  boolean supportsCommandProduce();
}
