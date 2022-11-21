package esthesis.dataflows.commandreplyupdater.config;

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
  Optional<String> kafkaGroup();

  // The Kafka topic to consume command reply messages from.
  String kafkaCommandReplyTopic();

  // The URL of the Kafka cluster to connect to.
  String kafkaClusterUrl();

  // The number of messages that can be queued for processing.
  @WithDefault("1000")
  int queueSize();

  // How often the queue is polled for new messages (in milliseconds).
  @WithDefault("100")
  int pollTimeout();

  // The maximum number of concurrent consumers.
  @WithDefault("10")
  int consumers();
}
