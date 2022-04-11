package esthesis.platform.backend.server.nifi.sinks.writers.influxdb;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PutInfluxDBConfiguration {

  @NotNull
  private String username;
  @NotNull
  private String password;
  @NotNull
  private String databaseName;
  @NotNull
  private String databaseUrl;
  private String retentionPolicy;
  private String maxConnectionTimeoutSeconds;
  private String consistencyLevel;
  private String charset;
  private String maxRecordSize;
  private String maxRecordSizeUnit;
  private String schedulingPeriod;
}
