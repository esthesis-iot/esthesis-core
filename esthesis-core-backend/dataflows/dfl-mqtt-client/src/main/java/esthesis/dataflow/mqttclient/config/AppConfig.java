package esthesis.dataflow.mqttclient.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

	// The URL of the MQTT broker.
	String mqttBrokerClusterUrl();

	// The interval at which to send MQTT PINGREQ packets to the broker, in seconds.
	int mqttBrokerKeepAliveInterval();

	// The filesystem path to the client certificate.
	Optional<String> mqttBrokerCert();

	// The filesystem path to the client key.
	Optional<String> mqttBrokerKey();

	// The filesystem path to the CA certificate.
	Optional<String> mqttBrokerCa();

	// The prefix of the MQTT Ping topic.
	Optional<String> mqttPingTopic();

	// The prefix of the MQTT Telemetry topic.
	Optional<String> mqttTelemetryTopic();

	// The prefix of the MQTT Metadata topic.
	Optional<String> mqttMetadataTopic();

	// The prefix of the MQTT Command Request topic.
	Optional<String> mqttCommandRequestTopic();

	// The prefix of the MQTT Command Reply topic.
	Optional<String> mqttCommandReplyTopic();

	// The URLs of the Kafka brokers.
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

	// The prefix of the Kafka Ping topic.
	Optional<String> kafkaPingTopic();

	// The prefix of the Kafka Telemetry topic.
	Optional<String> kafkaTelemetryTopic();

	// The prefix of the Kafka Metadata topic.
	Optional<String> kafkaMetadataTopic();

	// The prefix of the Kafka Command Request topic.
	Optional<String> kafkaCommandRequestTopic();

	// The prefix of the Kafka Command Reply topic.
	Optional<String> kafkaCommandReplyTopic();

}
