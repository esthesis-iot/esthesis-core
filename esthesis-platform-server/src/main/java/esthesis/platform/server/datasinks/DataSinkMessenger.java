package esthesis.platform.server.datasinks;

import esthesis.extension.platform.event.MQTTDataEvent;
import esthesis.extension.platform.event.MQTTMetadataEvent;
import esthesis.extension.platform.event.MQTTTelemetryEvent;
import esthesis.extension.platform.sink.EsthesisMetadataSink;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A helper component to send a value for persistence to a data sink asynchronously.
 */
@Component
public class DataSinkMessenger {
  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DataSinkMessenger.class.getName());
  private float pressureReleasePoint = 0.9f;

  @Async
  public void metadataMessage(EsthesisMetadataSink metadataSink, MQTTDataEvent event) {
    if (metadataSink.getPressure() < pressureReleasePoint) {
      metadataSink.processEvent((MQTTMetadataEvent) event);
    } else {
      LOGGER.log(Level.WARNING,
        "Discarded sending MQTT event {0} to data sink {1} as pressure was higher than {2}.",
        new Object[]{event.getId(), metadataSink.getFriendlyName(), pressureReleasePoint});
    }
  }

  @Async
  public void telemetryMessage(EsthesisTelemetrySink telemetrySink, MQTTDataEvent event) {
    if (telemetrySink.getPressure() < pressureReleasePoint) {
      telemetrySink.processEvent((MQTTTelemetryEvent) event);
    } else {
      LOGGER.log(Level.WARNING,
        "Discarded sending MQTT event {0} to data sink {1} as pressure was higher than {2}.",
        new Object[]{event.getId(), telemetrySink.getFriendlyName(), pressureReleasePoint});
    }
  }
}
