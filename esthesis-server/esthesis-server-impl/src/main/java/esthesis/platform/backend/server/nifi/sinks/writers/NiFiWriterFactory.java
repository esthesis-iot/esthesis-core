package esthesis.platform.backend.server.nifi.sinks.writers;

import esthesis.platform.backend.server.nifi.sinks.NiFiSinkFactory;

import java.io.IOException;

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

  /**
   *  Creates/deletes the connection to the Clear Queue processor.
   *  Connection is created when no writers of specific type are available and deleted when
   *  at least one exists.
   * @param path The path to the Instance group of the writer type.
   * @throws IOException
   */
  void manageConnectionWithClearQueueProcessor(String[] path) throws IOException;

}
