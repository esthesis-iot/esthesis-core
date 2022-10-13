package esthesis.dataflow.mqttclient.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  // The URL of the MQTT broker.
  String mqttBrokerClusterUrl();

  // The prefix of the MQTT Ping topic.
  String mqttTopicPing();

  // The prefix of the MQTT Telemetry topic.
  String mqttTopicTelemetry();

  // The prefix of the MQTT Metadata topic.
  String mqttTopicMetadata();

  // The prefix of the MQTT Control Request topic.
  String mqttTopicControlRequest();

  // The prefix of the MQTT Control Reply topic.
  String mqttTopicControlReply();

  // The URLs of the Kafka brokers.
  String kafkaClusterUrl();

  // The prefix of the Kafka Ping topic.
  String kafkaTopicPing();

  // The prefix of the Kafka Telemetry topic.
  String kafkaTopicTelemetry();

  // The prefix of the Kafka Metadata topic.
  String kafkaTopicMetadata();

  // The prefix of the Kafka Control Request topic.
  String kafkaTopicControlRequest();

  // The prefix of the Kafka Control Reply topic.
  String kafkaTopicControlReply();

}
