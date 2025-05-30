package esthesis.dataflow.mqttclient.service;

import esthesis.common.avro.AvroUtils;
import esthesis.common.avro.EsthesisCommandReplyMessage;
import esthesis.common.avro.EsthesisCommandRequestMessage;
import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.EsthesisDataMessage.Builder;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.exception.QMismatchException;
import esthesis.core.common.AppConstants;
import esthesis.dataflow.mqttclient.config.AppConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.paho.PahoConstants;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Utility class for processing messages received from the MQTT broker.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class DflMqttClientService {

	@Inject
	AppConfig config;

	@Inject
	AvroUtils avroUtils;

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	/**
	 * Determines the type of message received based on the topic that the message was received on.
	 *
	 * @param exchange The Camel exchange.
	 * @return The type of message received.
	 */
	@SuppressWarnings("java:S3655")
	private MessageTypeEnum getMessageType(Exchange exchange) {
		String topic = exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC, String.class);

		if (config.mqttPingTopic().isPresent() && topic.startsWith(config.mqttPingTopic().get())) {
			return MessageTypeEnum.P;
		} else if (config.mqttTelemetryTopic().isPresent() && topic.startsWith(
			config.mqttTelemetryTopic().get())) {
			return MessageTypeEnum.T;
		} else if (config.mqttMetadataTopic().isPresent() && topic.startsWith(
			config.mqttMetadataTopic().get())) {
			return MessageTypeEnum.M;
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
		log.debug("Received message '{}' on topic '{}'.",
			StringUtils.abbreviate(body, AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH),
			exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC));

		// Extract the message type and the hardware id of this message based on
		// the topic that the message was posted on.
		MessageTypeEnum messageType = getMessageType(exchange);
		String hardwareId = getHardwareId(exchange);

		// Add the hardware id as the Kafka key.
		exchange.getIn().setHeader(KafkaConstants.KEY, hardwareId);

		// Create an EsthesisMessage out of each line of the message content.
		List<EsthesisDataMessage> messages = new ArrayList<>();
		body.lines().filter(line -> !line.startsWith("#")).forEach(line -> {
			Builder esthesisDataMessageBuilder = EsthesisDataMessage.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setHardwareId(hardwareId)
				.setType(messageType)
				.setSeenAt(Instant.now().toString())
				.setChannel(exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC, String.class))
				.setSeenBy(appName);
			try {
				esthesisDataMessageBuilder.setPayload(avroUtils.parsePayload(line));
				EsthesisDataMessage msg = esthesisDataMessageBuilder.build();
				log.debug("Parsed message to Avro message '{}'.",
					StringUtils.abbreviate(msg.toString(), AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH));
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
		log.debug("Received Command Reply '{}'.",
			StringUtils.abbreviate(commandReply, AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH));

		// Parse the command reply message.
		exchange.getIn().setBody(
			avroUtils.parseCommandReplyLP(commandReply, getHardwareId(exchange), appName, topic));
	}

	/**
	 * Converts an {@link EsthesisCommandRequestMessage} to an ELP string.
	 *
	 * @param exchange The Camel exchange with the Avro message.
	 */
	public void commandRequestToLineProtocol(Exchange exchange) {
		EsthesisCommandRequestMessage msg = exchange.getIn()
			.getBody(EsthesisCommandRequestMessage.class);

		exchange.getIn().setBody(avroUtils.commandRequestToLineProtocol(msg));
	}

}
