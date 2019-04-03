package esthesis.platform.datasink.influxdb.sink;

import esthesis.extension.platform.event.MQTTMetadataEvent;
import esthesis.extension.platform.sink.EsthesisMetadataSink;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InfluxDBMetadataSink implements EsthesisMetadataSink {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(InfluxDBMetadataSink.class.getName());
  private String configuration;

  public InfluxDBMetadataSink(String configuration) {
    this.configuration = configuration;
    LOGGER.log(Level.FINE, "Instantiating InfluxDBMetadataSink.");
  }

  @Override
  public void processEvent(MQTTMetadataEvent event) {
    System.out.println("1: Got meta event! " + event.toString());
  }

  @Override
  public void disconnect() {
    LOGGER.log(Level.FINE, "Disconnecting InfluxDBMetadataSink.");
  }
}
