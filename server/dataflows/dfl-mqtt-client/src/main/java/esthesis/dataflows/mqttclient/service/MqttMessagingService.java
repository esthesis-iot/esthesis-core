package esthesis.dataflows.mqttclient.service;

import esthesis.dataflows.mqttclient.config.AppConfig;
import esthesis.dataflows.mqttclient.service.EsthesisMessage.MessageType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.kafka.KafkaConstants;

@Slf4j
@ApplicationScoped
public class MqttMessagingService {

  @Inject
  AppConfig config;

  private static final String HEADER_TOPIC = "CamelMqttTopic";

  private MessageType getMessageType(Exchange exchange) {
    String topic = exchange.getIn().getHeader(HEADER_TOPIC, String.class);
    if (topic.startsWith(config.mqttPingTopic())) {
      return MessageType.PING;
    } else if (topic.startsWith(config.mqttTelemetryTopic())) {
      return MessageType.TELEMETRY;
    } else if (topic.startsWith(config.mqttMetadataTopic())) {
      return MessageType.METADATA;
    } else if (topic.startsWith(config.mqttControlRequestTopic())) {
      return MessageType.CONTROL_REQUEST;
    } else if (topic.startsWith(config.mqttControlReplyTopic())) {
      return MessageType.CONTROL_REPLY;
    } else {
      throw new UnsupportedOperationException("Received a message "
          + "on unsupported topic: " + topic);
    }
  }

  private String getHardwareId(Exchange exchange) {
    MessageType messageType = getMessageType(exchange);
    return switch (messageType) {
      case PING -> exchange.getIn().getHeader(HEADER_TOPIC, String.class)
          .substring(config.mqttPingTopic().length() + 1);
      case TELEMETRY -> exchange.getIn().getHeader(HEADER_TOPIC, String.class)
          .substring(config.mqttTelemetryTopic().length() + 1);
      case METADATA -> exchange.getIn().getHeader(HEADER_TOPIC, String.class)
          .substring(config.mqttMetadataTopic().length() + 1);
      case CONTROL_REQUEST ->
          exchange.getIn().getHeader(HEADER_TOPIC, String.class)
              .substring(config.mqttControlRequestTopic().length() + 1);
      case CONTROL_REPLY ->
          exchange.getIn().getHeader(HEADER_TOPIC, String.class)
              .substring(config.mqttControlReplyTopic().length() + 1);
    };
  }

  private String getKafkaOutputTopic(MessageType messageType) {
    return switch (messageType) {
      case PING -> config.kafkaPingTopic();
      case TELEMETRY -> config.kafkaTelemetryTopic();
      case METADATA -> config.kafkaMetadataTopic();
      case CONTROL_REQUEST -> config.kafkaControlRequestTopic();
      case CONTROL_REPLY -> config.kafkaControlReplyTopic();
    };
  }

  public void process(Exchange exchange) throws Exception {
    List<EsthesisMessage> generatedMessages = new ArrayList<>();

    // Get the message body and process every line to generate messages.
    String body = exchange.getIn().getBody(String.class);
    String topic = exchange.getIn().getHeader(HEADER_TOPIC, String.class);
    MessageType messageType = getMessageType(exchange);
    String hardwareId = getHardwareId(exchange);
    exchange.getIn()
        .setHeader(KafkaConstants.TOPIC, getKafkaOutputTopic(messageType));
    exchange.getIn().setHeader(KafkaConstants.KEY, hardwareId);

    for (String line : body.split("\n")) {
      // Skip lines that don't start with "$".
      if (!line.startsWith("$")) {
        continue;
      }

      // Generate the message.
      EsthesisMessage msg = new EsthesisMessage();
      msg.setId(UUID.randomUUID().toString());
      msg.setChannel(topic);
      msg.setHardwareId(hardwareId);
      msg.setType(messageType);
      msg.setPayload(line);

      generatedMessages.add(msg);
    }

    exchange.getIn().setBody(generatedMessages);
  }
}
