package esthesis.dataflows.rdbmswriter.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  String dbKind();

  String dbUsername();

  String dbPassword();

  String dbUrl();

  String dbInsertType();

  String dbTableName();

  // The Kafka consumer group id.
  Optional<String> kafkaGroup();

  // The Kafka topic to consume telemetry messages from.
  Optional<String> kafkaTelemetryTopic();

  // The Kafka topic to consume metadata messages from.
  Optional<String> kafkaMetadataTopic();

  // The URL of the Kafka cluster to connect to.
  String kafkaClusterUrl();
}
