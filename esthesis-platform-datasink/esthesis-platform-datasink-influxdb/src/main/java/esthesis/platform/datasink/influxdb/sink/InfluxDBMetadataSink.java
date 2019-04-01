package esthesis.platform.datasink.influxdb.sink;

import esthesis.extension.platform.event.MQTTMetadataEvent;
import esthesis.extension.platform.sink.EsthesisMetadataSink;

public class InfluxDBMetadataSink implements EsthesisMetadataSink {
  private String config;

  @Override
  public void processEvent(MQTTMetadataEvent event) {
    System.out.println("1: Got meta event! " + event.toString());
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("1: processing ended");
  }

  @Override
  public void stop() {

  }
}
