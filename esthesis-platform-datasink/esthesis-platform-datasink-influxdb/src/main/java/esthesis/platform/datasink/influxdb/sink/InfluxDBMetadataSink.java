package esthesis.platform.datasink.influxdb.sink;

import esthesis.extension.config.AppConstants.MqttPayload;
import esthesis.extension.platform.event.MQTTMetadataEvent;
import esthesis.extension.platform.sink.EsthesisMetadataSink;

import java.util.logging.Logger;

public class InfluxDBMetadataSink extends InfluxDBSink implements EsthesisMetadataSink {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(InfluxDBMetadataSink.class.getName());
  public static final String SINK_NAME = "InfluxDBMetadataSink";

  public InfluxDBMetadataSink(String configuration) {
    super(configuration, SINK_NAME, MqttPayload.TAG_METADATA_NAME);
  }

  @Override
  public void processEvent(MQTTMetadataEvent event) {
    super.processEvent(event.getPayload(), event.getId(), event.getTopic());
  }
}
