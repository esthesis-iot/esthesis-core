package esthesis.platform.datasink.influxdb.factory;

import esthesis.extension.platform.sink.EsthesisDataSinkFactory;
import esthesis.extension.platform.sink.EsthesisMetadataSink;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;
import esthesis.platform.datasink.influxdb.sink.InfluxDBMetadataSink;
import esthesis.platform.datasink.influxdb.sink.InfluxDBTelemetrySink;

public class InfluxDBSinkFactory implements EsthesisDataSinkFactory {

  private final String DATA_SINK_NAME = "InfluxDB";
  private String configuration;

  @Override
  public EsthesisMetadataSink getMetadataSink() {
    return new InfluxDBMetadataSink(configuration);
  }

  @Override
  public EsthesisTelemetrySink getTelemetrySink() {
    return new InfluxDBTelemetrySink(configuration);
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
  public boolean supportsMetadata() {
    return true;
  }

  @Override
  public boolean supportsTelemetry() {
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
