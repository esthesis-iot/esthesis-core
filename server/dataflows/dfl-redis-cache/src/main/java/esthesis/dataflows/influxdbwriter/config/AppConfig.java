package esthesis.dataflows.influxdbwriter.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  // The URL of the redis cluster.
  String redisUrl();

  // The password to connect to redis.
  String redisPassword();

  // The number of seconds after which a Redis entry expires. Leave empty for
  // entries that never expire.
  Optional<Integer> redisExpiration();
  
  // The Kafka consumer group id.
  Optional<String> kafkaGroup();

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
  @WithDefault("10")
  int consumers();
}
