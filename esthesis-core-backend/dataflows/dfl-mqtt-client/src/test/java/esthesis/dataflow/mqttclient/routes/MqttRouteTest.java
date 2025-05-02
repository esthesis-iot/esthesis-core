package esthesis.dataflow.mqttclient.routes;

import esthesis.core.common.serder.kafka.EsthesisDataMessageSerializer;
import esthesis.dataflow.mqttclient.service.DflMqttClientService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@Slf4j
@QuarkusTest
@Disabled("Disabled until unexpected behavior is being investigated.")
class MqttRouteTest extends CamelQuarkusTestSupport {

	EsthesisDataMessageSerializer esthesisDataMessageSerializer = new EsthesisDataMessageSerializer();

	@InjectMock
	DflMqttClientService dflMqttClientService;


	@BeforeEach
	@SneakyThrows
	void setup() {
		// Log the currently registered Camel routes to assist with identifying route IDs.
		context.getRoutes().forEach(
			route -> log.info("Route id: {}, Route uri: {}", route.getId(),
				route.getEndpoint().getEndpointUri()));

		// Replace the MQTT telemetry to kafka endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "mqtt-telemetry-to-kafka", a -> {
			a.replaceFromWith("direct:test-mqtt-telemetry-to-kafka");
		});

		// Replace the MQTT metadata to kafka endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "mqtt-metadata-to-kafka", a -> {
			a.replaceFromWith("direct:test-mqtt-metadata-to-kafka");
		});

		// Replace the MQTT Ping to kafka endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "mqtt-ping-to-kafka", a -> {
			a.replaceFromWith("direct:test-mqtt-ping-to-kafka");
		});

		// Replace the MQTT command reply to kafka endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "mqtt-command-reply-to-kafka", a -> {
			a.replaceFromWith("direct:test-mqtt-command-reply-to-kafka");
		});

		// Replace the MQTT command request to kafka endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "mqtt-command-request-to-kafka", a -> {
			a.replaceFromWith("direct:test-mqtt-command-request-to-kafka");
		});

		// Prevent the actual execution of the InfluxDBService logic during the test.
		doNothing().when(dflMqttClientService).processCommandReplyMessage(any());
		doNothing().when(dflMqttClientService).toEsthesisDataMessages(any());
		doNothing().when(dflMqttClientService).commandRequestToLineProtocol(any());



	}

	@Test
	@SneakyThrows
	void testRoutes() {
		assertNotNull(dflMqttClientService);
		//TODO: Add test cases for the routes.
	}
}
