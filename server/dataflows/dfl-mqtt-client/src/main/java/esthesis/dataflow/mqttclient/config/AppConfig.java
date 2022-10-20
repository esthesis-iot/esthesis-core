package esthesis.dataflow.mqttclient.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  // The URL of the MQTT broker.
  String mqttBrokerClusterUrl();

  // The prefix of the MQTT Ping topic.
  Optional<String> mqttTopicPing();

  // The prefix of the MQTT Telemetry topic.
  Optional<String> mqttTopicTelemetry();

  // The prefix of the MQTT Metadata topic.
  Optional<String> mqttTopicMetadata();

  // The prefix of the MQTT Control Request topic.
  Optional<String> mqttTopicControlRequest();

  // The prefix of the MQTT Control Reply topic.
  Optional<String> mqttTopicControlReply();

  // The URLs of the Kafka brokers.
  String kafkaClusterUrl();

  // The prefix of the Kafka Ping topic.
  Optional<String> kafkaTopicPing();

  // The prefix of the Kafka Telemetry topic.
  Optional<String> kafkaTopicTelemetry();

  // The prefix of the Kafka Metadata topic.
  Optional<String> kafkaTopicMetadata();

  // The prefix of the Kafka Control Request topic.
  Optional<String> kafkaTopicControlRequest();

  // The prefix of the Kafka Control Reply topic.
  Optional<String> kafkaTopicControlReply();

}
