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
    super(configuration, SINK_NAME, MqttPayload.TAG_EVENT_TYPE_METADATA_NAME);
  }

  @Override
  public void processEvent(MQTTMetadataEvent event) {
    processEvent(event.getHardwareId(), event.getPayload(), event.getId(), event.getTopic());
  }

  @Override
  public float getPressure() {
    return super.getPressure();
  }

  @Override
  public String getFriendlyName() {
    return getSinkName();
  }
}
