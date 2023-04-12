package esthesis.dataflows.rediscache.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  // The URL of the redis cluster.
  String redisUrl();

  // The maximum value size (in bytes) eligible for caching.
  int redisMaxSize();

  // TTL (in minutes)
  long redisTtl();

  // The Kafka consumer group id.
  Optional<String> kafkaConsumerGroup();

  // The Kafka topic to consume telemetry messages from.
  Optional<String> kafkaTelemetryTopic();

  // The Kafka topic to consume metadata messages from.
  Optional<String> kafkaMetadataTopic();

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
