package esthesis.dataflow.mqttclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.dataflow.common.messages.EsthesisMessage;
import esthesis.dataflow.common.messages.EsthesisMessage.MessageType;
import esthesis.dataflow.mqttclient.config.AppConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.component.kafka.KafkaConstants;

@ApplicationScoped
public class DflMqttClientService {

  @Inject
  AppConfig config;

  @Inject
  ObjectMapper objectMapper;

  private static final String HEADER_TOPIC = "CamelMqttTopic";

  private MessageType getMessageType(Exchange exchange) {
    String topic = exchange.getIn().getHeader(HEADER_TOPIC, String.class);
    if (config.mqttTopicPing().isEmpty() && topic.startsWith(
        config.mqttTopicPing().get())) {
      return MessageType.PING;
    } else if (config.mqttTopicTelemetry().isPresent() && topic.startsWith(
        config.mqttTopicTelemetry().get())) {
      return MessageType.TELEMETRY;
    } else if (config.mqttTopicMetadata().isPresent() && topic.startsWith(
        config.mqttTopicMetadata().get())) {
      return MessageType.METADATA;
    } else if (config.mqttTopicControlRequest().isPresent() && topic.startsWith(
        config.mqttTopicControlRequest().get())) {
      return MessageType.CONTROL_REQUEST;
    } else if (config.mqttTopicControlReply().isPresent() && topic.startsWith(
        config.mqttTopicControlReply().get())) {
      return MessageType.CONTROL_REPLY;
    } else {
      throw new UnsupportedOperationException("Received a message "
          + "on unsupported topic '" + topic + "'.");
    }
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private String getHardwareId(Exchange exchange) {
    MessageType messageType = getMessageType(exchange);

    return switch (messageType) {
      case PING -> exchange.getIn().getHeader(HEADER_TOPIC, String.class)
          .substring(config.mqttTopicPing().get().length() + 1);
      case TELEMETRY -> exchange.getIn().getHeader(HEADER_TOPIC, String.class)
          .substring(config.mqttTopicTelemetry().get().length() + 1);
      case METADATA -> exchange.getIn().getHeader(HEADER_TOPIC, String.class)
          .substring(config.mqttTopicMetadata().get().length() + 1);
      case CONTROL_REQUEST ->
          exchange.getIn().getHeader(HEADER_TOPIC, String.class)
              .substring(config.mqttTopicControlRequest().get().length() + 1);
      case CONTROL_REPLY ->
          exchange.getIn().getHeader(HEADER_TOPIC, String.class)
              .substring(config.mqttTopicControlReply().get().length() + 1);
    };
  }

  public void process(Exchange exchange) throws JsonProcessingException {
    List<String> generatedMessages = new ArrayList<>();

    // Get the message body and process every line to generate messages.
    String body = exchange.getIn().getBody(String.class);
    String topic = exchange.getIn().getHeader(HEADER_TOPIC, String.class);
    MessageType messageType = getMessageType(exchange);
    String hardwareId = getHardwareId(exchange);
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

      generatedMessages.add(objectMapper.writeValueAsString(msg));
    }

    exchange.getIn().setBody(generatedMessages);
  }
}
