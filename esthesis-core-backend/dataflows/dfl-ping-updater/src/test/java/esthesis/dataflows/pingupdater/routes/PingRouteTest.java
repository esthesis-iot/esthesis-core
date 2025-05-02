package esthesis.dataflows.pingupdater.routes;

import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.PayloadData;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import esthesis.core.common.serder.kafka.EsthesisDataMessageSerializer;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@Slf4j
@QuarkusTest
class PingRouteTest extends CamelQuarkusTestSupport {

	EsthesisDataMessageSerializer esthesisDataMessageSerializer = new EsthesisDataMessageSerializer();

	@BeforeEach
	void setup() throws Exception {
		// Print available routes and their URIs for debugging.
		context.getRoutes().forEach(
			route -> log.info("Route id: {}, Route uri: {}", route.getId(),
				route.getEndpoint().getEndpointUri()));

		// Replace the Kafka consumer with a direct endpoint for controlled message injection in tests.
		AdviceWith.adviceWith(context, "route1", a -> {
			a.replaceFromWith("direct:test-input");
			// Mock 'seda:' endpoints to verify intermediate message flow.
			a.mockEndpoints("seda:*");
		});

		// Mock MongoDB endpoint to verify messages without actual DB interactions.
		AdviceWith.adviceWith(context, "route2", a -> {
			a.mockEndpoints("mongodb:*");
		});
	}

	@Test
	void testRoute() throws Exception {
		// Mock endpoint representing intermediate processing queue.
		MockEndpoint mockSeda = getMockEndpoint("mock:seda:ping");

		// Mock MongoDB endpoint to verify final message persistence step.
		MockEndpoint mockMongo = getMockEndpoint("mock:mongodb:camelMongoClient");

		// Prepare expectation for one message to reach the seda:ping route.
		mockSeda.expectedMessageCount(1);
		// Prepare expectation for one message to be sent to MongoDB after processing.
		mockMongo.expectedMessageCount(1);

		// Create a valid test message simulating what would come from Kafka, serialized to byte[].
		byte[] message = esthesisDataMessageSerializer.serialize(
			"test-topic",
			EsthesisDataMessage.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setHardwareId("test-hardware")
				.setType(esthesis.common.avro.MessageTypeEnum.P)
				.setChannel("test-channel")
				.setSeenAt("2025-04-29T12:00:00Z")
				.setSeenBy("test-seen-by")
				.setCorrelationId("test-correlation-id")
				.setPayload(new PayloadData("ping", "2025-04-29T12:00:00Z",
					List.of(new ValueData("ping", "2025-04-29T12:00:00Z", ValueTypeEnum.STRING))))
				.build());

		// Inject test message into the route.
		template.sendBody("direct:test-input", message);

		// Ensure that the message was received by the intermediate processing step.
		mockSeda.assertIsSatisfied();
		// Ensure that the processed message was also routed to the MongoDB endpoint.
		mockMongo.assertIsSatisfied();
	}
}
