package esthesis.dataflows.commandreplyupdater.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  // The name of the database to update.
  String esthesisDbName();

  // The Kafka consumer group id.
  Optional<String> kafkaConsumerGroup();

  // The Kafka topic to consume command reply messages from.
  String kafkaCommandReplyTopic();

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
