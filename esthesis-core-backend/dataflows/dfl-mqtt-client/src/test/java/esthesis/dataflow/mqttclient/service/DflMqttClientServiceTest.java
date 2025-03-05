package esthesis.dataflow.mqttclient.service;

import esthesis.common.avro.AvroUtils;
import esthesis.common.avro.CommandType;
import esthesis.common.avro.EsthesisCommandReplyMessage;
import esthesis.common.avro.EsthesisCommandRequestMessage;
import esthesis.common.avro.ExecutionType;
import esthesis.common.avro.PayloadData;
import esthesis.common.avro.ReplyType;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import esthesis.dataflow.mqttclient.config.AppConfig;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.paho.PahoConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DflMqttClientServiceTest {

	@Mock
	private AppConfig config;

	@Mock
	private Exchange exchange;

	@Mock
	private Message message;

	@Mock
	AvroUtils avroUtils;

	@InjectMocks
	DflMqttClientService dflMqttClientService;

	@BeforeEach
	void setUp() {
		// Initialize mocks.
		MockitoAnnotations.openMocks(this);

		// Set up common mock behaviors.
		when(exchange.getIn()).thenReturn(message);

		// Set application name.
		dflMqttClientService.appName = "esthesis-core-dfl-mqtt-client-test";
	}

	@Test
	void toEsthesisDataMessages() {
		// Arrange: Set up the valid headers and payload.
		String validPayload = "categoryTest measurementOne=1i,measurementTwo=2.0f";
		when(config.mqttTelemetryTopic()).thenReturn(Optional.of("telemetry/testHardwareId"));
		when(message.getHeader(PahoConstants.MQTT_TOPIC, String.class)).thenReturn("telemetry/testHardwareId");
		when(message.getHeader(PahoConstants.MQTT_TOPIC)).thenReturn("telemetry/testHardwareId");
		when(avroUtils.parsePayload(anyString())).thenReturn(createPayload());
		when(message.getBody(String.class)).thenReturn(validPayload);

		// Act: Call the method to convert the payload to EsthesisDataMessages.
		dflMqttClientService.toEsthesisDataMessages(exchange);

		// Assert: Verify if hardwareId is set as the Kafka key.
		verify(message).setHeader(KafkaConstants.KEY, "testHardwareId");

		// Assert: Verify if the body is set as a list of EsthesisDataMessage.
		verify(message).setBody(any(List.class));
	}

	@Test
	void processCommandReplyMessage() {
		// Arrange: Set up the message headers and body.
		when(message.getHeader(PahoConstants.MQTT_TOPIC, String.class)).thenReturn("telemetry/testHardwareId");
		when(message.getHeader(PahoConstants.MQTT_TOPIC)).thenReturn("command/reply/testHardwareId");
		when(message.getBody(String.class)).thenReturn("correlationId s test-payload");
		when(avroUtils.parseCommandReplyLP(anyString(), anyString(), anyString(), anyString()))
			.thenReturn(createEsthesisCommandReplyMessage());

		// Act: Call the method to process the command reply message.
		dflMqttClientService.processCommandReplyMessage(exchange);

		// Assert: Verify if hardwareId is set as the Kafka key.
		verify(message).setHeader(KafkaConstants.KEY, "testHardwareId");

		// Assert: Verify if the command reply message is parsed correctly.
		verify(avroUtils).parseCommandReplyLP(
			"correlationId s test-payload",
			"testHardwareId",
			"esthesis-core-dfl-mqtt-client-test",
			"command/reply/testHardwareId");

		// Assert: Verify if the body is set as an EsthesisCommandReplyMessage.
		verify(message).setBody(any(EsthesisCommandReplyMessage.class));
	}

	@Test
	void commandRequestToLineProtocol() {
		// Arrange: Set up the command request message.
		when(message.getBody(EsthesisCommandRequestMessage.class)).thenReturn(createEsthesisCommandRequestMessage());
		when(avroUtils.commandRequestToLineProtocol(any(EsthesisCommandRequestMessage.class)))
			.thenReturn("testCommand testArguments");

		// Act: Call the method to convert the command request to line protocol.
		dflMqttClientService.commandRequestToLineProtocol(exchange);

		// Assert: Verify if the body is set as a line protocol string.
		verify(message).setBody(any(String.class));
	}

	private PayloadData createPayload() {
		return PayloadData.newBuilder()
			.setCategory("categoryTest")
			.setTimestamp(Instant.now().toString())
			.setValues(
				List.of(
					new ValueData("measurementOne", "1i", ValueTypeEnum.BIG_INTEGER),
					new ValueData("measurementTwo", "10.01f", ValueTypeEnum.DOUBLE)))
			.build();
	}

	private EsthesisCommandReplyMessage createEsthesisCommandReplyMessage() {
		return EsthesisCommandReplyMessage.newBuilder()
			.setId("testId")
			.setChannel("testChannel")
			.setCorrelationId("testCorrelationId")
			.setPayload("testPayload")
			.setType(ReplyType.s)
			.setSeenAt(Instant.now().toString())
			.setSeenBy("testSeenBy")
			.setHardwareId("testHardwareId")
			.build();
	}

	private EsthesisCommandRequestMessage createEsthesisCommandRequestMessage() {
		return EsthesisCommandRequestMessage.newBuilder()
			.setId("testId")
			.setCommand("testCommand")
			.setArguments("testArguments")
			.setCreatedAt(Instant.now().toString())
			.setCommandType(CommandType.e)
			.setExecutionType(ExecutionType.s)
			.setHardwareId("testHardwareId")
			.build();
	}
}
