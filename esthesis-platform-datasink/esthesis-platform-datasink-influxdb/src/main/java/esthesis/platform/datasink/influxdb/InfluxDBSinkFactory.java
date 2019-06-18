package esthesis.platform.datasink.influxdb;


import esthesis.extension.common.config.AppConstants.Mqtt;
import esthesis.extension.datasink.DataSink;
import esthesis.extension.datasink.DataSinkFactory;

public class InfluxDBSinkFactory implements DataSinkFactory {

  private final static String DATA_SINK_NAME = "InfluxDB";
  private String configuration;

  @Override
  public DataSink getMetadataSink() {
    return new InfluxDBDataSink(configuration, DATA_SINK_NAME + "/" + Mqtt.EventType.METADATA,
      Mqtt.EventType.METADATA);
  }

  @Override
  public DataSink getTelemetrySink() {
    return new InfluxDBDataSink(configuration, DATA_SINK_NAME + "/" + Mqtt.EventType.TELEMETRY,
      Mqtt.EventType.TELEMETRY);
  }

  @Override
  public String getFriendlyName() {
    return DATA_SINK_NAME;
  }

  @Override
  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  @Override
  public boolean supportsMetadataWrite() {
    return true;
  }

  @Override
  public boolean supportsTelemetryWrite() {
    return true;
  }

  @Override
  public boolean supportsMetadataRead() {
    return true;
  }

  @Override
  public boolean supportsTelemetryRead() {
    return true;
  }

  @Override
  public String getConfigurationTemplate() {
    return
      "username: \n" +
        "password: \n" +
        "databaseName: \n" +
        "databaseUrl: \n" +
        "retentionPolicy: \n" +
        "queueSize: ";
  }
}
