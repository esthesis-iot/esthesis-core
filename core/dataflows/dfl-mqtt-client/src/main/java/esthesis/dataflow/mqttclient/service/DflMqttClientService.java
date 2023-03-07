package esthesis.dataflow.mqttclient.service;

import static esthesis.dataflow.common.DflUtils.abbr;

import esthesis.avro.EsthesisCommandReplyMessage;
import esthesis.avro.EsthesisCommandRequestMessage;
import esthesis.avro.EsthesisDataMessage;
import esthesis.avro.EsthesisDataMessage.Builder;
import esthesis.avro.MessageType;
import esthesis.common.exception.QMismatchException;
import esthesis.dataflow.common.AvroUtils;
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
import org.apache.camel.component.paho.PahoConstants;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class DflMqttClientService {

  @Inject
  AppConfig config;

  @Inject
  AvroUtils avroUtils;

  @ConfigProperty(name = "quarkus.application.name")
  String appName;

  @SuppressWarnings("java:S3655")
  private MessageType getMessageType(Exchange exchange) {
    String topic = exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC, String.class);

    if (config.mqttTopicPing().isPresent() && topic.startsWith(config.mqttTopicPing().get())) {
      return MessageType.P;
    } else if (config.mqttTopicTelemetry().isPresent() && topic.startsWith(
        config.mqttTopicTelemetry().get())) {
      return MessageType.T;
    } else if (config.mqttTopicMetadata().isPresent() && topic.startsWith(
        config.mqttTopicMetadata().get())) {
      return MessageType.M;
    } else {
      throw new UnsupportedOperationException(
          "Received a data message " + "on an unsupported topic '" + topic + "'.");
    }
  }

  /**
   * Extracts the hardware ID of the device sending this messages based on the name of the MQTT
   * topic this message was received on.
   *
   * @param exchange The Camel exchange.
   */
  private String getHardwareId(Exchange exchange) {
    String topic = exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC, String.class);
    return topic.substring(topic.lastIndexOf("/") + 1);
  }

  /**
   * Converts one or more of esthesis line protocol messages to a list of
   * {@link EsthesisDataMessage}.
   *
   * @param exchange The Camel exchange with the text form of the message.
   */
  public void toEsthesisDataMessages(Exchange exchange) {
    String body = exchange.getIn().getBody(String.class);
    log.debug("Received message '{}' on topic '{}'.", abbr(body),
        exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC));

    // Extract the message type and the hardware id of this message based on
    // the topic that the message was posted on.
    MessageType messageType = getMessageType(exchange);
    String hardwareId = getHardwareId(exchange);

    // Add the hardware id as the Kafka key.
    exchange.getIn().setHeader(KafkaConstants.KEY, hardwareId);

    // Create an EsthesisMessage out of each line of the message content.
    List<EsthesisDataMessage> messages = new ArrayList<>();
    body.lines().filter(line -> !line.startsWith("#")).forEach(line -> {
      Builder esthesisMessageBuilder = EsthesisDataMessage.newBuilder()
          .setId(UUID.randomUUID().toString()).setHardwareId(hardwareId).setType(messageType)
          .setSeenAt(Instant.now().toString())
          .setChannel(exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC, String.class))
          .setSeenBy(appName);
      try {
        esthesisMessageBuilder.setPayload(avroUtils.parsePayload(line));
        EsthesisDataMessage msg = esthesisMessageBuilder.build();
        log.debug("Parsed message to Avro message '{}'.", abbr(msg.toString()));
        messages.add(msg);
      } catch (QMismatchException e) {
        log.warn(e.getMessage());
      }
    });

    exchange.getIn().setBody(messages);
  }

  /**
   * Parses a command reply expressed in esthesis line protocol format to an
   * {@link EsthesisCommandReplyMessage}.
   *
   * @param exchange
   */
  public void processCommandReplyMessage(Exchange exchange) {
    // Set the Kafka key for this message based on the topic name this
    // message was received in.
    String topic = exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC).toString();
    exchange.getIn().setHeader(KafkaConstants.KEY, topic.substring(topic.lastIndexOf("/") + 1));

    String commandReply = exchange.getIn().getBody(String.class);
    log.debug("Received Command Reply '{}'.", abbr(commandReply));

    // Parse the command reply message.
    exchange.getIn().setBody(
        avroUtils.parseCommandReplyLP(commandReply, getHardwareId(exchange), appName, topic));
  }

  public void commandRequestToLineProtocol(Exchange exchange) {
    EsthesisCommandRequestMessage msg = exchange.getIn()
        .getBody(EsthesisCommandRequestMessage.class);

    exchange.getIn().setBody(avroUtils.commandRequestToLineProtocol(msg));
  }
}
