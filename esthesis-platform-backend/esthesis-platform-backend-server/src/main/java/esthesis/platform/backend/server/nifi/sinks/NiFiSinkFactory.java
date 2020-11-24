package esthesis.platform.backend.server.nifi.sinks;

import esthesis.platform.backend.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.backend.server.model.NiFiSink;

import java.io.IOException;

/**
 * Common functionality for all types of data.
 */
public interface NiFiSinkFactory {

  /**
   * Returns the friendly name of the underlying data processor to be displayed in the UI of the
   * platform.
   */
  String getFriendlyName();

  /**
   * Returns a template for the configuration parameters required for this data processor. This is
   * to be provided as a helper to the users trying to configure this data consumer.
   */
  String getConfigurationTemplate();

  /**
   * Creates the NiFi sink and returns it's id.
   *
   * @param niFiSinkDTO An object containing all required properties.
   */
  void createSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException;

  /**
   * Updates a NiFi sink
   *
   * @param sink The existing sink.
   * @param sinkDTO An object containing the updates of the sink.
   * @param path
   * @return The id of the updated sink.
   */
  String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO, String[] path) throws IOException;

  /**
   * Deletes a NiFi sink.
   *
   * @param niFiSinkDTO The id of the NiFi sink that will be deleted.
   * @param path
   */
  void deleteSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException;

  /**
   * Toggles the state of the sink.
   *
   * @param name The id of the NiFi sink to toggle.
   * @param path
   * @param isEnabled Whether to enable or disable the sink.
   * @return The id of the toggled sink.
   */
  String toggleSink(String name, String[] path, boolean isEnabled) throws IOException;

  /**
   * Enables the controller services of a sink
   *
   * @param controllerServices The id(s) of the the service(s).
   */
  void enableControllerServices(String... controllerServices) throws IOException;

  /**
   * Gets the validation errors of a sink.
   *
   * @param name The id of the sink.
   * @param path
   * @return A String containing all validation errors.
   */
  String getSinkValidationErrors(String name, String[] path) throws IOException;

  boolean exists(String name, String[] path) throws IOException;

  boolean isSinkRunning(String name, String[] path) throws IOException;
}
