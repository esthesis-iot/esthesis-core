package esthesis.platform.datasink.influxdb.sink;

import esthesis.extension.platform.event.MQTTTelemetryEvent;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;

public class InfluxDBTelemetrySink implements EsthesisTelemetrySink {

  @Override
  public void mqttTelemetryEvent(MQTTTelemetryEvent event) {
    System.out.println("Got telemetry event! " + event.toString());
  }
}
