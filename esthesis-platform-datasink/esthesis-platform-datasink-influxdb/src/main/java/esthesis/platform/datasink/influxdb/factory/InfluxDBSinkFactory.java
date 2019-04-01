package esthesis.platform.datasink.influxdb.factory;

import esthesis.extension.platform.sink.EsthesisDataSinkFactory;
import esthesis.extension.platform.sink.EsthesisMetadataSink;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;
import esthesis.platform.datasink.influxdb.sink.InfluxDBMetadataSink;
import esthesis.platform.datasink.influxdb.sink.InfluxDBTelemetrySink;

public class InfluxDBSinkFactory implements EsthesisDataSinkFactory {

  @Override
  public EsthesisMetadataSink getMetadataSink() {
    return new InfluxDBMetadataSink();
  }

  @Override
  public EsthesisTelemetrySink getTelemetrySink() {
    return new InfluxDBTelemetrySink();
  }

  @Override
  public String getFriendlyName() {
    return "InfluxDB data sink";
  }

  @Override
  public void setConfiguration(String configuration) {
    System.out.println("Got config: " + configuration);
  }


}
