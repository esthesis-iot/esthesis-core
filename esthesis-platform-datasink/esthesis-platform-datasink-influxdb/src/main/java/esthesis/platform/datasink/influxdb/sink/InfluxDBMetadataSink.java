package esthesis.platform.datasink.influxdb.sink;

import esthesis.extension.config.AppConstants.Mqtt;
import esthesis.extension.platform.datasink.DataSinkDataPoint;
import esthesis.extension.platform.datasink.MetadataSink;
import esthesis.extension.platform.event.MQTTMetadataEvent;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

public class InfluxDBMetadataSink extends InfluxDBSink implements MetadataSink {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(InfluxDBMetadataSink.class.getName());
  public static final String SINK_NAME = "InfluxDBMetadataSink";

  public InfluxDBMetadataSink(String configuration) {
    super(configuration, SINK_NAME, Mqtt.EventType.METADATA);
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

  @Override
  public List<DataSinkDataPoint> getDataPoints(String hardwareId, String measurement, Instant fromDate,
    Instant toDate, String... fields) {
    super.getData(hardwareId, measurement);

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
