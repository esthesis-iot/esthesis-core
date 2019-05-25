package esthesis.platform.datasink.influxdb.config;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InfluxDBConfiguration {
  @NotNull
  private String username;
  @NotNull
  private String password;
  @NotNull
  private String databaseName;
  @NotNull
  private String databaseUrl;
  private String retentionPolicy;
  private int queueSize = 1000;
}
