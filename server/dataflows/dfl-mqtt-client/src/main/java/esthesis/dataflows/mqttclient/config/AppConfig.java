package esthesis.dataflows.mqttclient.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl.mqtt-client")
public interface AppConfig {

  // The URL of the MQTT broker.
  String mqttBrokerUrl();

  // The URLs of the Kafka brokers.
  String kafkaBrokers();

  // The prefix of the MQTT Ping topic.
  String mqttPingTopic();

  // The prefix of the MQTT Telemetry topic.
  String mqttTelemetryTopic();

  // The prefix of the MQTT Metadata topic.
  String mqttMetadataTopic();

  // The prefix of the MQTT Control Request topic.
  String mqttControlRequestTopic();

  // The prefix of the MQTT Control Reply topic.
  String mqttControlReplyTopic();

  // The prefix of the Kafka Ping topic.
  String kafkaPingTopic();

  // The prefix of the Kafka Telemetry topic.
  String kafkaTelemetryTopic();

  // The prefix of the Kafka Metadata topic.
  String kafkaMetadataTopic();

  // The prefix of the Kafka Control Request topic.
  String kafkaControlRequestTopic();

  // The prefix of the Kafka Control Reply topic.
  String kafkaControlReplyTopic();

}
