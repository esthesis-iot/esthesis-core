package esthesis.backend.nifi.sinks.producers.influxdb;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class ExecuteInfluxDBConfiguration {

  private String username;
  private String password;
  @NotNull
  private String databaseName;
  @NotNull
  private String databaseUrl;
  private String maxConnectionTimeoutSeconds;
  private String queryResultTimeUnit;
  private String queryChunkSize;
  private String schedulingPeriod;

}
