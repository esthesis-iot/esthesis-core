package esthesis.platform.datasink.influxdb.sink;

import esthesis.extension.platform.event.MQTTTelemetryEvent;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InfluxDBTelemetrySink implements EsthesisTelemetrySink {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(InfluxDBTelemetrySink.class.getName());
  private String configuration;

  public InfluxDBTelemetrySink(String configuration) {
    this.configuration = configuration;
    LOGGER.log(Level.FINE, "Instantiating InfluxDBTelemetrySink.");
  }

  @Override
  public void processEvent(MQTTTelemetryEvent event) {
    System.out.println("Got telemetry event! " + event.toString());
  }

  @Override
  public void disconnect() {
    LOGGER.log(Level.FINE, "Disconnecting InfluxDBTelemetrySink.");
  }
}
