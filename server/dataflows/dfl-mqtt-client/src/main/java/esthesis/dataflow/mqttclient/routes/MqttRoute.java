package esthesis.dataflow.mqttclient.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflow.mqttclient.config.AppConfig;
import esthesis.dataflow.mqttclient.service.DflMqttClientService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

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

    log.info("Using MQTT broker '{}'.", config.mqttBrokerClusterUrl());
    log.info("Using Kafka broker '{}'.", config.kafkaClusterUrl());

    // @formatter:off
    if (config.mqttTopicTelemetry().isPresent() && config.kafkaTopicTelemetry().isPresent()) {
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          config.mqttTopicTelemetry(), config.kafkaTopicTelemetry());
      from("paho:" + config.mqttTopicTelemetry() + "/#" + "?brokerUrl="
          + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "process")
          .split(body())
          .toD("kafka:" + config.kafkaTopicTelemetry()
              + "?brokers=" + config.kafkaClusterUrl());
    }
    if (config.mqttTopicMetadata().isPresent() && config.kafkaTopicMetadata().isPresent()) {
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          config.mqttTopicMetadata(), config.kafkaTopicMetadata());
      from("paho:" + config.mqttTopicMetadata() + "/#" + "?brokerUrl="
          + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "process")
          .split(body())
          .toD("kafka:" + config.kafkaTopicMetadata()
              + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicPing().isPresent() && config.kafkaTopicPing().isPresent()) {
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          config.mqttTopicPing(), config.kafkaTopicPing());
      from("paho:" + config.mqttTopicPing() + "/#" + "?brokerUrl="
          + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "process")
          .split(body())
          .toD("kafka:" + config.kafkaTopicPing()
              + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicControlReply().isPresent() && config.kafkaTopicControlReply().isPresent()) {
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          config.mqttTopicControlReply(), config.kafkaTopicControlReply());
      from("paho:" + config.mqttTopicControlReply() + "/#" + "?brokerUrl="
          + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "process")
          .split(body())
          .toD("kafka:" + config.kafkaTopicControlReply()
              + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicControlRequest().isPresent() && config.kafkaTopicControlRequest().isPresent()) {
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          config.mqttTopicControlRequest(), config.kafkaTopicControlRequest());
      from("paho:" + config.mqttTopicControlRequest() + "/#" + "?brokerUrl="
          + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "process")
          .split(body())
          .toD("kafka:" + config.kafkaTopicControlRequest()
              + "?brokers=" + config.kafkaClusterUrl());
      }
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
