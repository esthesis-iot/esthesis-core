package esthesis.dataflows.rdbmswriter.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  enum STORAGE_STRATEGY {
    SINGLE, MULTI
  }

  String dbKind();

  String dbJdbcUrl();
  
  STORAGE_STRATEGY dbStorageStrategy();

  @WithDefault("esthesis_key")
  String dbStorageStrategySingleKeyName();

  @WithDefault("esthesis_value")
  String dbStorageStrategySingleValueName();

  @WithDefault("esthesis_timestamp")
  String dbStorageStrategySingleTimestampName();

  @WithDefault("esthesis_hardware_id")
  String dbStorageStrategySingleHardwareIdName();

  @WithDefault("measurements")
  String dbStorageStrategySingleTableName();

  @WithDefault("esthesis_timestamp")
  String dbStorageStrategyMultiTimestampName();

  @WithDefault("esthesis_hardware_id")
  String dbStorageStrategyMultiHardwareIdName();

  // The Kafka consumer group id.
  Optional<String> kafkaGroup();

  // The Kafka topic to consume telemetry messages from.
  Optional<String> kafkaTelemetryTopic();

  // The Kafka topic to consume metadata messages from.
  Optional<String> kafkaMetadataTopic();

  // The URL of the Kafka cluster to connect to.
  String kafkaClusterUrl();

  @WithDefault("1000")
  int queueSize();

  @WithDefault("500")
  int pollTimeout();

  @WithDefault("10")
  int consumers();
}
