package esthesis.dataflow.mqttclient.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

	// The URL of the MQTT broker.
	String mqttBrokerClusterUrl();

	// The filesystem path to the client certificate.
	String mqttBrokerCert();

	// The filesystem path to the client key.
	String mqttBrokerKey();

	// The filesystem path to the CA certificate.
	String mqttBrokerCa();

	// The prefix of the MQTT Ping topic.
	Optional<String> mqttTopicPing();

	// The prefix of the MQTT Telemetry topic.
	Optional<String> mqttTopicTelemetry();

	// The prefix of the MQTT Metadata topic.
	Optional<String> mqttTopicMetadata();

	// The prefix of the MQTT Command Request topic.
	Optional<String> mqttTopicCommandRequest();

	// The prefix of the MQTT Command Reply topic.
	Optional<String> mqttTopicCommandReply();

	// The URLs of the Kafka brokers.
	String kafkaClusterUrl();

	// The prefix of the Kafka Ping topic.
	Optional<String> kafkaTopicPing();

	// The prefix of the Kafka Telemetry topic.
	Optional<String> kafkaTopicTelemetry();

	// The prefix of the Kafka Metadata topic.
	Optional<String> kafkaTopicMetadata();

	// The prefix of the Kafka Command Request topic.
	Optional<String> kafkaTopicCommandRequest();

	// The prefix of the Kafka Command Reply topic.
	Optional<String> kafkaTopicCommandReply();

}
