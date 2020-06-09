package esthesis.platform.server.nifi.sinks.producers.influxdb;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class ExecuteInfluxDBConfiguration {

  @NotNull
  private String databaseName;
  @NotNull
  private String databaseUrl;
  private String maxConnectionTimeoutSeconds;
  private String queryResultTimeUnit;
  private String query;
  private String queryChunkSize;

}
