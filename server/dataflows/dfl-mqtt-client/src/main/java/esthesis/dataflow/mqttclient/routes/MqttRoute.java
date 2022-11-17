package esthesis.dataflow.mqttclient.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflow.common.AvroUtils;
import esthesis.dataflow.mqttclient.config.AppConfig;
import esthesis.dataflow.mqttclient.service.DflMqttClientService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
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

    // @formatter:off
    if (config.mqttTopicTelemetry().isPresent() && config.kafkaTopicTelemetry().isPresent()) {
      String mqttTopic = config.mqttTopicTelemetry().get();
      String kafkaTopic = config.kafkaTopicTelemetry().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "toEsthesisDataMessages")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
          .toD("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicMetadata().isPresent() && config.kafkaTopicMetadata().isPresent()) {
      String mqttTopic = config.mqttTopicMetadata().get();
      String kafkaTopic = config.kafkaTopicMetadata().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "toEsthesisDataMessages")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
          .toD("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicPing().isPresent() && config.kafkaTopicPing().isPresent()) {
      String mqttTopic = config.mqttTopicPing().get();
      String kafkaTopic = config.kafkaTopicPing().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "toEsthesisDataMessages")
          .split(body())
          .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
          .to("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicCommandReply().isPresent() && config.kafkaTopicCommandReply().isPresent()) {
      String mqttTopic = config.mqttTopicCommandReply().get();
      String kafkaTopic = config.kafkaTopicCommandReply().get();
      log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.",
          mqttTopic, kafkaTopic);
      from("paho:" + mqttTopic + "/#" + "?brokerUrl=" + config.mqttBrokerClusterUrl())
          .bean(dflMqttClientService, "processCommandReplyMessage")
//          .marshal(new AvroDataFormat("esthesis.avro.EsthesisCommandReplyMessage"))
          .bean(dflMqttClientService, "test")
          .toD("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl());
    }

    if (config.mqttTopicCommandRequest().isPresent() && config.kafkaTopicCommandRequest().isPresent()) {
      String mqttTopic = config.mqttTopicCommandRequest().get();
      String kafkaTopic = config.kafkaTopicCommandRequest().get();
      log.info("Creating route from Kafka topic '{}' to MQTT topic '{}'.",
          kafkaTopic, mqttTopic);
      from("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl())
          .setHeader(PahoConstants.CAMEL_PAHO_OVERRIDE_TOPIC,
              constant(mqttTopic).append("/").append(header(KafkaConstants.KEY)))
          .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisCommandRequestMessage"))
          .bean(dflMqttClientService, "commandRequestToLineProtocol")
          .to("paho:dynamic?brokerUrl=" + config.mqttBrokerClusterUrl());
      }
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
