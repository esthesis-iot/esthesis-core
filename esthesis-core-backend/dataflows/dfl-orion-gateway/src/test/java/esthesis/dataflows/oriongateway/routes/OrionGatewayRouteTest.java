package esthesis.dataflows.oriongateway.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.avro.PayloadData;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import esthesis.core.common.serder.kafka.EsthesisDataMessageSerializer;
import esthesis.dataflows.oriongateway.service.OrionClientService;
import esthesis.dataflows.oriongateway.service.OrionGatewayService;
import esthesis.dataflows.oriongateway.service.OrionMessagingService;
import esthesis.util.kafka.notifications.common.AppMessage;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@QuarkusTest
class OrionGatewayRouteTest extends CamelQuarkusTestSupport {

	@InjectMock
	OrionMessagingService orionMessagingService;

	@InjectMock
	OrionGatewayService orionGatewayService;

	// This will inject the mocked service (MockOrionClientService.class) instead of the real one.
	@Inject
	OrionClientService orionClientService;

	EsthesisDataMessageSerializer esthesisDataMessageSerializer = new EsthesisDataMessageSerializer();


	@BeforeEach
	@SneakyThrows
	void setup() {
		// Log the currently registered Camel routes to assist with identifying route IDs.
		context.getRoutes().forEach(
			route -> log.info("Route id: {}, Route uri: {}", route.getId(),
				route.getEndpoint().getEndpointUri()));

		// Replace the Kafka application topic endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "route1", a -> {
			a.replaceFromWith("direct:test-app");
		});

		// Replace the Kafka telemetry topic endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "route3", a -> {
			a.replaceFromWith("direct:test-telemetry");
		});

		// Replace the Kafka metadata topic endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "route5", a -> {
			a.replaceFromWith("direct:test-metadata");
		});

		doNothing().when(orionMessagingService).onAppMessage(any());
		doNothing().when(orionGatewayService).processData(any());


	}

	@Test
	@SneakyThrows
	void testRoutes() {

		// Build a sample application message to simulate a Kafka payload.
		String appMessage =
			new ObjectMapper().writeValueAsString(AppMessage.builder()
				.component(KafkaNotificationsConstants.Component.DEVICE)
				.subject(KafkaNotificationsConstants.Subject.DASHBOARD)
				.action(KafkaNotificationsConstants.Action.CREATE)
				.msgId("test-msg-id")
				.targetId("test-target-id")
				.comment("test-comment")
				.broadcast(false)
				.build()
			);

		// Build a sample telemetry message to simulate a Kafka payload.
		byte[] telemetryMessage = esthesisDataMessageSerializer.serialize(
			"test-topic-telemetry",
			EsthesisDataMessage.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setHardwareId("test-hardware")
				.setType(MessageTypeEnum.T)
				.setChannel("test-channel")
				.setSeenAt("2025-05-01T12:00:00Z")
				.setSeenBy("test-seen-by")
				.setCorrelationId("test-correlation-id")
				.setPayload(new PayloadData("test", "2025-05-01T12:00:00Z",
					List.of(new ValueData("test", "10", ValueTypeEnum.INTEGER))))
				.build());

		// Build a sample metadata message to simulate a Kafka payload.
		byte[] metadataMessage = esthesisDataMessageSerializer.serialize(
			"test-topic-metadata",
			EsthesisDataMessage.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setHardwareId("test-hardware")
				.setType(MessageTypeEnum.M)
				.setChannel("test-channel")
				.setSeenAt("2025-05-01T12:00:00Z")
				.setSeenBy("test-seen-by")
				.setCorrelationId("test-correlation-id")
				.setPayload(new PayloadData("test", "2025-05-01T12:00:00Z",
					List.of(new ValueData("test", "test-value", ValueTypeEnum.STRING))))
				.build());


		// Send the test app message through the direct endpoint, simulating a Kafka event.
		template.sendBody("direct:test-app", appMessage);

		// Send the test telemetry message through the direct endpoint, simulating a Kafka event.
		template.sendBody("direct:test-telemetry", telemetryMessage);

		// Send the test metadata message through the direct endpoint, simulating a Kafka event.
		template.sendBody("direct:test-metadata", metadataMessage);


		// Verify that the app message was processed as expected.
		verify(orionMessagingService, times(1)).onAppMessage(any());

		// Verify that the telemetry and metadata messages were processed as expected.
		verify(orionGatewayService, times(2)).processData(any());
	}


}
