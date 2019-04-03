package esthesis.platform.server.datasinks;

import esthesis.extension.platform.event.MQTTDataEvent;
import esthesis.extension.platform.event.MQTTMetadataEvent;
import esthesis.extension.platform.event.MQTTTelemetryEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class DataSinkDistributor implements ApplicationListener<MQTTDataEvent> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DataSinkDistributor.class.getName());
  private final DataSinkMessenger sinkMessenger;

  public DataSinkDistributor(DataSinkMessenger sinkMessenger) {
    this.sinkMessenger = sinkMessenger;
  }

  @Override
  public void onApplicationEvent(MQTTDataEvent event) {
    System.out.println("distributor: " + event);
    if (event instanceof MQTTMetadataEvent) {
      //      System.out.println("META!!!!!!!!!!!!!!");
      //      metadataSinks.entrySet().parallelStream().forEach(sink -> {
      //        sinkMessenger.message(sink.getValue(), event);
      //        //        sink.getValue().processEvent((MQTTMetadataEvent)event);
      //      });
    } else if (event instanceof MQTTTelemetryEvent) {
      System.out.println("TELE");
    }
    System.out.println("distributor ended");
  }
}
