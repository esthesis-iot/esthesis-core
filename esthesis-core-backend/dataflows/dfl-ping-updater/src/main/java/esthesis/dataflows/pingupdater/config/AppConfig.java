package esthesis.dataflows.pingupdater.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  // The name of the database to update.
  String esthesisDbName();

  // The Kafka consumer group id.
  Optional<String> kafkaConsumerGroup();

  // The Kafka topic to consume ping messages from.
  String kafkaPingTopic();

  // The URL of the Kafka cluster to connect to.
  String kafkaClusterUrl();

  // The number of messages that can be queued for processing.
  @WithDefault("1000")
  int queueSize();

  // How often the queue is polled for new messages (in milliseconds).
  @WithDefault("500")
  int pollTimeout();

  // The maximum number of concurrent consumers.
  @WithDefault("4")
  int consumers();
}
