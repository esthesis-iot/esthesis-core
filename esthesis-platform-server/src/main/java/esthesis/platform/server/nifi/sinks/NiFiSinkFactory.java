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
   * Creates the nifi sink and returns it's id.
   *
   * @param niFiSinkDTO An object containing all required properties.
   * @return the id of the newly created sink.
   */
  NiFiSinkDTO createSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException;

  String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO) throws IOException;

  String deleteSink(String id) throws IOException;

  String toggleSink(String id, boolean isEnabled) throws IOException;

  void enableControllerServices(String... controllerServices) throws IOException;

  String getSinkValidationErrors(String id) throws IOException;
}
