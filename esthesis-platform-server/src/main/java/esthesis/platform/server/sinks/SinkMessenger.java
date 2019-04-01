package esthesis.platform.server.sinks;

import esthesis.extension.platform.event.MQTTDataEvent;
import esthesis.extension.platform.event.MQTTMetadataEvent;
import esthesis.extension.platform.sink.EsthesisMetadataSink;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SinkMessenger {

  @Async
  public void message(EsthesisMetadataSink metadataSink, MQTTDataEvent event) {
    metadataSink.processEvent((MQTTMetadataEvent)event);
  }
}
