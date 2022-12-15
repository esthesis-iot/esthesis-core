package esthesis.dataflow.mqttclient.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflow.common.AvroUtils;
import esthesis.dataflow.mqttclient.config.AppConfig;
import esthesis.dataflow.mqttclient.service.DflMqttClientService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.paho.PahoConstants;
import org.apache.camel.model.dataformat.AvroDataFormat;

@Slf4j
@ApplicationScoped
public class MqttRoute extends RouteBuilder {

  @Inject
  DflMqttClientService dflMqttClientService;

  @Inject
  AvroUtils avroUtils;

  @Inject
  AppConfig config;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-mqtt-client");

    // Configure Kafka.
    ComponentsBuilderFactory.kafka()
        .brokers(config.kafkaClusterUrl())
        .valueDeserializer(
            "org.apache.kafka.common.serialization.ByteArrayDeserializer")
        .valueSerializer(
            "org.apache.kafka.common.serialization.ByteArraySerializer")
        .register(getContext(), "kafka");

    // @formatter:off
    // TODO add logging
    if (config.mqttTopicTelemetry().isPresent() && config.kafkaTopicTelemetry().isPresent()) {
      String mqttTopic = config.mqttTopicTelemetry().get();
      String kafkaTopic = config.kafkaTopicTelemetry().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "toEsthesisDataMessages")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
          .toD("kafka:" + kafkaTopic);
    }

    // TODO add logging
    if (config.mqttTopicMetadata().isPresent() && config.kafkaTopicMetadata().isPresent()) {
      String mqttTopic = config.mqttTopicMetadata().get();
      String kafkaTopic = config.kafkaTopicMetadata().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "toEsthesisDataMessages")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
          .toD("kafka:" + kafkaTopic);
    }

    // TODO add logging
    if (config.mqttTopicPing().isPresent() && config.kafkaTopicPing().isPresent()) {
      String mqttTopic = config.mqttTopicPing().get();
      String kafkaTopic = config.kafkaTopicPing().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "toEsthesisDataMessages")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
          .to("kafka:" + kafkaTopic);
    }

    // TODO add logging
    if (config.mqttTopicCommandReply().isPresent() && config.kafkaTopicCommandReply().isPresent()) {
      String mqttTopic = config.mqttTopicCommandReply().get();
      String kafkaTopic = config.kafkaTopicCommandReply().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "processCommandReplyMessage")
          .marshal(new AvroDataFormat("esthesis.avro.EsthesisCommandReplyMessage"))
          .toD("kafka:" + kafkaTopic);
    }

    if (config.mqttTopicCommandRequest().isPresent() && config.kafkaTopicCommandRequest().isPresent()) {
      String mqttTopic = config.mqttTopicCommandRequest().get();
      String kafkaTopic = config.kafkaTopicCommandRequest().get();
      log.info("Creating route from Kafka topic '{}' to MQTT topic '{}'.",
          kafkaTopic, mqttTopic);
      from("kafka:" + kafkaTopic)
          .setHeader(PahoConstants.CAMEL_PAHO_OVERRIDE_TOPIC,
              constant(mqttTopic).append("/").append(header(KafkaConstants.KEY)))
          .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisCommandRequestMessage"))
          .log(LoggingLevel.DEBUG, log, "Received command request message '${body}'.")
          .bean(dflMqttClientService, "commandRequestToLineProtocol")
          .log(LoggingLevel.DEBUG, log, "Sending command request message '${body}' via MQTT.")
          .to("paho:dynamic?brokerUrl=" + config.mqttBrokerClusterUrl());
      }
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
