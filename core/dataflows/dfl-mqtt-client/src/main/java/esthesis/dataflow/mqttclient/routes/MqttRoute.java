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
  @SuppressWarnings({"java:S1192", "java:S1602"})
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
    config.mqttTopicTelemetry().ifPresentOrElse(mqttTopic -> {
      config.kafkaTopicTelemetry().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
            mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
            .bean(dflMqttClientService, "toEsthesisDataMessages")
            .split(body())
            .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
            .toD("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka telemetry topic is not set."));
    }, () -> log.debug("MQTT telemetry topic is not set."));

    config.mqttTopicMetadata().ifPresentOrElse(mqttTopic -> {
      config.kafkaTopicMetadata().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
            mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
            .bean(dflMqttClientService, "toEsthesisDataMessages")
            .split(body())
            .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
            .toD("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka metadata topic is not set."));
    }, () -> log.debug("MQTT metadata topic is not set."));

    config.mqttTopicPing().ifPresentOrElse(mqttTopic -> {
      config.kafkaTopicPing().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
            mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
            .bean(dflMqttClientService, "toEsthesisDataMessages")
            .split(body())
            .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
            .to("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka ping topic is not set."));
    }, () -> log.debug("MQTT ping topic is not set."));

    config.mqttTopicCommandReply().ifPresentOrElse(mqttTopic -> {
      config.kafkaTopicCommandReply().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
            mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
            .bean(dflMqttClientService, "processCommandReplyMessage")
            .marshal(new AvroDataFormat("esthesis.avro.EsthesisCommandReplyMessage"))
            .toD("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka command reply topic is not set."));
    }, () -> log.debug("MQTT command reply topic is not set."));

    config.mqttTopicCommandRequest().ifPresentOrElse(mqttTopic -> {
      config.kafkaTopicCommandRequest().ifPresentOrElse(kafkaTopic -> {
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
      }, () -> log.debug("Kafka command request topic is not set."));
    }, () -> log.debug("MQTT command request topic is not set."));
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
