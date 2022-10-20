package esthesis.dataflows.influxdbwriter.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  // The URL of the InfluxDB database to connect to.
  String influxUrl();

  // The access token to use.
  String influxToken();

  // The name of the organization to use.
  String influxOrg();

  // The name of the bucket to use.
  String influxBucket();

  // The Kafka consumer group id.
  Optional<String> kafkaGroup();

  // The Kafka topic to consume telemetry messages from.
  Optional<String> kafkaTelemetryTopic();

  // The Kafka topic to consume metadata messages from.
  Optional<String> kafkaMetadataTopic();

  // The URL of the Kafka cluster to connect to.
  String kafkaClusterUrl();
}
