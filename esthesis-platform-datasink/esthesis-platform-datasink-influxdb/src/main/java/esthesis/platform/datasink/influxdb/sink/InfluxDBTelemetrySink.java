package esthesis.platform.datasink.influxdb.sink;

import esthesis.extension.config.AppConstants.Mqtt.EventType;
import esthesis.extension.platform.datasink.DataSinkDataPoint;
import esthesis.extension.platform.datasink.TelemetrySink;
import esthesis.extension.platform.event.MQTTTelemetryEvent;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

public class InfluxDBTelemetrySink extends InfluxDBSink implements TelemetrySink {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(InfluxDBTelemetrySink.class.getName());
  public static final String SINK_NAME = "InfluxDBTelemetrySink";

  public InfluxDBTelemetrySink(String configuration) {
    super(configuration, SINK_NAME, EventType.TELEMETRY);
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

  @Override
  public List<DataSinkDataPoint> getDataPoints(String hardwareId, String measurement, Instant fromDate,
    Instant toDate, String... fields) {
    return null;
  }

  @Override
  public List<DataSinkDataPoint> getDataPoints(String hardwareId, String measurement, Instant fromDate,
    Instant toDate, int resultsPage, int resultsPageSize, String... fields) {
    return null;
  }

  @Override
  public long countDataPoints(String hardwareId, String measurement, Instant fromDate,
    Instant toDate, String... fields) {
    return 0;
  }

  @Override
  public long countDataPoints(String hardwareId, String measurement, Instant fromDate,
    Instant toDate, int resultsPage, int resultsPageSize, String... fields) {
    return 0;
  }
}
