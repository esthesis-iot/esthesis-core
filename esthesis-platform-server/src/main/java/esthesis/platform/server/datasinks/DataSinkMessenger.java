package esthesis.platform.server.datasinks;

import esthesis.extension.datasink.DataSink;
import esthesis.extension.datasink.MQTTDataEvent;
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
  public void processMessage(DataSink dataSink, MQTTDataEvent event) {
    if (dataSink.getPressure() < pressureReleasePoint) {
      dataSink.processEvent(event);
    } else {
      LOGGER.log(Level.WARNING,
        "Discarded sending MQTT event {0} to data sink {1} as pressure was higher than {2}.",
        new Object[]{event.getId(), dataSink.getFriendlyName(), pressureReleasePoint});
    }
  }
}
