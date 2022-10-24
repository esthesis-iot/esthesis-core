package esthesis.dataflow.mqttclient.service;

import esthesis.common.exception.QMismatchException;
import esthesis.dataflow.common.DflUtils;
import esthesis.dataflow.common.parser.EsthesisMessage;
import esthesis.dataflow.common.parser.EsthesisMessage.Builder;
import esthesis.dataflow.common.parser.MessageType;
import esthesis.dataflow.mqttclient.config.AppConfig;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.kafka.KafkaConstants;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class DflMqttClientService {

  private static final String CAMEL_HEADER_TOPIC = "CamelMqttTopic";

  @Inject
  AppConfig config;

  @Inject
  DflUtils dflUtils;

  @ConfigProperty(name = "quarkus.application.name")
  String appName;

  private MessageType getMessageType(Exchange exchange) {
    String topic = exchange.getIn().getHeader(CAMEL_HEADER_TOPIC, String.class);
    if (config.mqttTopicPing().isPresent() && topic.startsWith(
        config.mqttTopicPing().get())) {
      return MessageType.P;
    } else if (config.mqttTopicTelemetry().isPresent() && topic.startsWith(
        config.mqttTopicTelemetry().get())) {
      return MessageType.T;
    } else if (config.mqttTopicMetadata().isPresent() && topic.startsWith(
        config.mqttTopicMetadata().get())) {
      return MessageType.M;
    } else if (config.mqttTopicControlRequest().isPresent() && topic.startsWith(
        config.mqttTopicControlRequest().get())) {
      return MessageType.CQ;
    } else if (config.mqttTopicControlReply().isPresent() && topic.startsWith(
        config.mqttTopicControlReply().get())) {
      return MessageType.CA;
    } else {
      throw new UnsupportedOperationException("Received a message "
          + "on an unsupported topic '" + topic + "'.");
    }
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private String getHardwareId(Exchange exchange) {
    MessageType messageType = getMessageType(exchange);

    return switch (messageType) {
      case P -> exchange.getIn().getHeader(CAMEL_HEADER_TOPIC, String.class)
          .substring(config.mqttTopicPing().get().length() + 1);
      case T -> exchange.getIn().getHeader(CAMEL_HEADER_TOPIC, String.class)
          .substring(config.mqttTopicTelemetry().get().length() + 1);
      case M -> exchange.getIn().getHeader(CAMEL_HEADER_TOPIC, String.class)
          .substring(config.mqttTopicMetadata().get().length() + 1);
      case CQ -> exchange.getIn().getHeader(CAMEL_HEADER_TOPIC, String.class)
          .substring(config.mqttTopicControlRequest().get().length() + 1);
      case CA -> exchange.getIn().getHeader(CAMEL_HEADER_TOPIC, String.class)
          .substring(config.mqttTopicControlReply().get().length() + 1);
    };
  }

  /**
   * Convert an esthesis message in text form to an {@link EsthesisMessage}.
   *
   * @param exchange The Camel exchange with the text form of the message.
   */
  public void process(Exchange exchange) {
    log.debug("Received message '{}' on topic '{}'.",
        exchange.getIn().getBody(String.class),
        exchange.getIn().getHeader(CAMEL_HEADER_TOPIC));

    // Extract the message type and the hardware id of this message based on
    // the topic that the message was posted on.
    MessageType messageType = getMessageType(exchange);
    String hardwareId = getHardwareId(exchange);

    // Add the hardware id as the Kafka key.
    exchange.getIn().setHeader(KafkaConstants.KEY, hardwareId);

    // Create an EsthesisMessage out of each line of the message content.
    List<EsthesisMessage> messages = new ArrayList<>();
    exchange.getIn().getBody(String.class).lines()
        .filter(line -> !line.startsWith("#")).forEach(line -> {
          Builder esthesisMessageBuilder =
              EsthesisMessage.newBuilder()
                  .setId(UUID.randomUUID().toString())
                  .setHardwareId(hardwareId)
                  .setType(messageType)
                  .setSeenAt(Instant.now().toString())
                  .setChannel(
                      exchange.getIn().getHeader(CAMEL_HEADER_TOPIC, String.class))
                  .setSeenBy(appName);
          try {
            esthesisMessageBuilder.setPayload(dflUtils.parsePayload(line));
            log.debug("Parsed message to Avro message '{}'.",
                esthesisMessageBuilder.build().toString());
            messages.add(esthesisMessageBuilder.build());
          } catch (QMismatchException e) {
            log.warn(e.getMessage());
          }
        });

    exchange.getIn().setBody(messages);
  }
}
