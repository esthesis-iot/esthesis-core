package esthesis.platform.server.nifi.sinks;

import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;

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
   * @return the id of the newly created sink.
   */
  NiFiSinkDTO createSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException;

  /**
   * Updates a NiFi sink
   *
   * @param sink The existing sink.
   * @param sinkDTO An object containing the updates of the sink.
   * @return The id of the updated sink.
   */
  String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO) throws IOException;

  /**
   * Deletes a NiFi sink.
   *
   * @param niFiSinkDTO The id of the NiFi sink that will be deleted.
   * @return The id of the deleted .
   */
  String deleteSink(NiFiSinkDTO niFiSinkDTO) throws IOException;

  /**
   * Toggles the state of the sink.
   *
   * @param id The id of the NiFi sink to toggle.
   * @param isEnabled Whether to enable or disable the sink.
   * @return The id of the toggled sink.
   */
  String toggleSink(String id, boolean isEnabled) throws IOException;

  /**
   * Enables the controller services of a sink
   *
   * @param controllerServices The id(s) of the the service(s).
   */
  void enableControllerServices(String... controllerServices) throws IOException;

  /**
   * Gets the validation errors of a sink.
   *
   * @param id The id of the sink.
   * @return A String containing all validation errors.
   */
  String getSinkValidationErrors(String id) throws IOException;

  boolean exists(String id) throws IOException;

  boolean isSinkRunning(String id) throws IOException;
}
