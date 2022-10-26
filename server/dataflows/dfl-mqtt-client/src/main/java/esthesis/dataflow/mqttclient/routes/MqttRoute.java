package esthesis.dataflow.mqttclient.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflow.mqttclient.config.AppConfig;
import esthesis.dataflow.mqttclient.service.DflMqttClientService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.AvroDataFormat;

@Slf4j
@ApplicationScoped
public class MqttRoute extends RouteBuilder {

  @Inject
  DflMqttClientService dflMqttClientService;

  @Inject
  AppConfig config;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-mqtt-client");

    // @formatter:off
    if (config.mqttTopicTelemetry().isPresent() && config.kafkaTopicTelemetry().isPresent()) {
      String mqttTopic = config.mqttTopicTelemetry().get();
      String kafkaTopic = config.kafkaTopicTelemetry().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "process")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.dataflow.common.parser.EsthesisMessage"))
          .toD("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicMetadata().isPresent() && config.kafkaTopicMetadata().isPresent()) {
      String mqttTopic = config.mqttTopicMetadata().get();
      String kafkaTopic = config.kafkaTopicMetadata().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "process")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.dataflow.common.parser.EsthesisMessage"))
          .toD("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicPing().isPresent() && config.kafkaTopicPing().isPresent()) {
      String mqttTopic = config.mqttTopicPing().get();
      String kafkaTopic = config.kafkaTopicPing().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "process")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.dataflow.common.parser.EsthesisMessage"))
          .to("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicControlReply().isPresent() && config.kafkaTopicControlReply().isPresent()) {
      String mqttTopic = config.mqttTopicControlReply().get();
      String kafkaTopic = config.kafkaTopicControlReply().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "process")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.dataflow.common.parser.EsthesisMessage"))
          .toD("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicControlRequest().isPresent() && config.kafkaTopicControlRequest().isPresent()) {
      String mqttTopic = config.mqttTopicControlRequest().get();
      String kafkaTopic = config.kafkaTopicControlRequest().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "process")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.dataflow.common.parser.EsthesisMessage"))
          .toD("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl());
      }
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
