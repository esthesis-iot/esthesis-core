package esthesis.dataflows.commandreplyupdater.routes;

import esthesis.common.avro.EsthesisCommandReplyMessage;
import esthesis.common.avro.ReplyType;
import esthesis.dataflows.commandreplyupdater.service.CommandReplyUpdaterService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@QuarkusTest
class CommandReplyUpdaterRouteTest extends CamelQuarkusTestSupport {

	@InjectMock
	CommandReplyUpdaterService commandReplyUpdaterService;

	@BeforeEach
	@SneakyThrows
	void setup() {
		// Prevent the actual execution of the InfluxDBService logic during the test.
		doNothing().when(commandReplyUpdaterService).createMongoEntity(any());

		// Log the currently registered Camel routes to assist with identifying route IDs.
		context.getRoutes().forEach(
			route -> log.info("Route id: {}, Route uri: {}", route.getId(),
				route.getEndpoint().getEndpointUri()));

		// Replace the Kafka command reply endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "route1", a -> {
			a.replaceFromWith("direct:test-command-reply");
		});
	}

	@Test
	@SneakyThrows
	void testRoutes() {

		// Build a sample reply message to simulate a Kafka payload.
		byte[] message =
			EsthesisCommandReplyMessage
				.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setChannel("test-channel")
				.setCorrelationId("test-correlation-id")
				.setPayload("test-payload")
				.setHardwareId("test-hardware-id")
				.setType(ReplyType.s)
				.setSeenAt("2025-05-01T12:00:00Z")
				.setSeenBy("test-seen-by")
				.build()
				.toByteBuffer()
				.array();


		// Send the test message through the direct endpoint, simulating a Kafka event.
		template.sendBody("direct:test-command-reply", message);

		// Verify that each message was processed as expected.
		verify(commandReplyUpdaterService, times(1)).createMongoEntity(any());

	}
}
