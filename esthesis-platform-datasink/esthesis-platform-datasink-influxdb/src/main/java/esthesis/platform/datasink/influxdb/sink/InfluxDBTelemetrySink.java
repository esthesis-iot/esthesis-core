package esthesis.platform.datasink.influxdb.sink;

import esthesis.extension.config.AppConstants.MqttPayload;
import esthesis.extension.platform.event.MQTTTelemetryEvent;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;

import java.util.logging.Logger;

public class InfluxDBTelemetrySink extends InfluxDBSink implements EsthesisTelemetrySink {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(InfluxDBTelemetrySink.class.getName());
  public static final String SINK_NAME = "InfluxDBTelemetrySink";

  public InfluxDBTelemetrySink(String configuration) {
    super(configuration, SINK_NAME, MqttPayload.TAG_EVENT_TYPE_TELEMETRY_NAME);
  }

  @Override
  public void processEvent(MQTTTelemetryEvent event) {
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
