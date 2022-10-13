package esthesis.dataflows.pingupdater.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  // The name of the database to update.
  String esthesisDbName();

  // The Kafka consumer group id.
  Optional<String> kafkaGroup();

  // The Kafka topic to consume ping messages from.
  String kafkaPingTopic();

  // The URL of the Kafka cluster to connect to.
  String kafkaClusterUrl();
}
