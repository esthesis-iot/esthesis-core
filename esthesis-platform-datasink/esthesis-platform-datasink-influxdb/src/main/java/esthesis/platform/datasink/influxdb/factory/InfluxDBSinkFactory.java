package esthesis.platform.datasink.influxdb.factory;

import esthesis.extension.platform.datasink.DataSinkFactory;
import esthesis.extension.platform.datasink.MetadataSink;
import esthesis.extension.platform.datasink.TelemetrySink;
import esthesis.platform.datasink.influxdb.sink.InfluxDBMetadataSink;
import esthesis.platform.datasink.influxdb.sink.InfluxDBTelemetrySink;

public class InfluxDBSinkFactory implements DataSinkFactory {

  private final String DATA_SINK_NAME = "InfluxDB";
  private String configuration;

  @Override
  public MetadataSink getMetadataSink() {
    return new InfluxDBMetadataSink(configuration);
  }

  @Override
  public TelemetrySink getTelemetrySink() {
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
