package esthesis.platform.server.datasinks;

import esthesis.extension.platform.event.MQTTDataEvent;
import esthesis.extension.platform.event.MQTTMetadataEvent;
import esthesis.extension.platform.event.MQTTTelemetryEvent;
import esthesis.extension.platform.sink.EsthesisMetadataSink;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DataSinkMessenger {

  @Async
  public void metadataMessage(EsthesisMetadataSink metadataSink, MQTTDataEvent event) {
    metadataSink.processEvent((MQTTMetadataEvent) event);
  }

  @Async
  public void telemetryMessage(EsthesisTelemetrySink telemetrySink, MQTTDataEvent event) {
    telemetrySink.processEvent((MQTTTelemetryEvent) event);
  }
}
