package esthesis.dataflows.rediscache.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
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

	// The security protocol to use when connecting to the Kafka cluster. If empty, no security
	// protocol will be setup.
	Optional<String> kafkaSecurityProtocol();

	// The SASL mechanism to use when connecting to the Kafka cluster. If empty, no SASL mechanism
	// will be setup.
	Optional<String> kafkaSaslMechanism();

	// The JAAS configuration to use when connecting to the Kafka cluster. If empty, no JAAS
	// configuration will be setup.
	Optional<String> kafkaJaasConfig();

	// The number of messages that can be queued for processing.
	int concurrencyQueueSize();

	// How often the queue is polled for new messages (in milliseconds).
	int concurrencyPollTimeout();

	// The maximum number of concurrent consumers.
	int concurrencyConsumers();
}
