package esthesis.platform.datasink.influxdb.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InfluxDBConfiguration {
  private String username;
  private String password;
  private String databaseName;
  private String databaseUrl;
  private String retentionPolicyName;
  private String retentionPolicyDuration;
  private String shardDuration;
  private int replicationFactor;
}
