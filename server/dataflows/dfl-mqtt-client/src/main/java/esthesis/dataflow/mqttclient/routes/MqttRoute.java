package esthesis.dataflow.mqttclient.routes;

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

    from("paho:" + config.mqttTopicTelemetry() + "/#" + "?brokerUrl="
        + config.mqttBrokerClusterUrl())
        .bean(dflMqttClientService, "process")
        .split(body())
        .toD("kafka:" + config.kafkaTopicTelemetry()
            + "?brokers=" + config.kafkaClusterUrl());

    from("paho:" + config.mqttTopicMetadata() + "/#" + "?brokerUrl="
        + config.mqttBrokerClusterUrl())
        .bean(dflMqttClientService, "process")
        .split(body())
        .toD("kafka:" + config.kafkaTopicMetadata()
            + "?brokers=" + config.kafkaClusterUrl());

    from("paho:" + config.mqttTopicPing() + "/#" + "?brokerUrl="
        + config.mqttBrokerClusterUrl())
        .bean(dflMqttClientService, "process")
        .split(body())
        .toD("kafka:" + config.kafkaTopicPing()
            + "?brokers=" + config.kafkaClusterUrl());

    from("paho:" + config.mqttTopicControlReply() + "/#" + "?brokerUrl="
        + config.mqttBrokerClusterUrl())
        .bean(dflMqttClientService, "process")
        .split(body())
        .toD("kafka:" + config.kafkaTopicControlReply()
            + "?brokers=" + config.kafkaClusterUrl());

    from("paho:" + config.mqttTopicControlRequest() + "/#" + "?brokerUrl="
        + config.mqttBrokerClusterUrl())
        .bean(dflMqttClientService, "process")
        .split(body())
        .toD("kafka:" + config.kafkaTopicControlRequest()
            + "?brokers=" + config.kafkaClusterUrl());

    log.info("Routes created successfully.");
  }
}
