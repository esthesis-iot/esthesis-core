package esthesis.common.datasink;

/**
 * A factory interface to be implemented by data sink implementations. This is a generic
 * factory contract allowing data sink to provide different implementations for telemetry and
 * metadata handlers.
 */
public interface DataSinkFactory {

  /**
   * Returns a handler for metadata data.
   */
  DataSink getMetadataSink();

  /**
   * Returns a handler for telemetry data.
   */
  DataSink getTelemetrySink();

  /**
   * Returns the friendly name of the underlying data sink to be displayed in the UI of the
   * platform.
   */
  String getFriendlyName();

  /**
   * Sets the configuration of this data sink.
   */
  void setConfiguration(String configuration);

  /**
   * Returns whether this data sink is capable to persist metadata data.
   */
  boolean supportsMetadataWrite();

  /**
   * Returns whether this data sink is capable to persist telemetry data.
   */
  boolean supportsTelemetryWrite();

  /**
   * Returns whether this data sink is capable to retrieve and return metadata data.
   */
  boolean supportsMetadataRead();

  /**
   * Returns whether this data sink is capable to retrieve and return telemetry data.
   */
  boolean supportsTelemetryRead();

  /**
   * Returns a template for the configuration parameters required for this data sink. This is to
   * be provided as a helper to the users trying to configure this data sink.
   */
  String getConfigurationTemplate();
}
