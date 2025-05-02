package esthesis.dataflows.rediscache.routes;

import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.avro.PayloadData;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import esthesis.core.common.serder.kafka.EsthesisDataMessageSerializer;
import esthesis.dataflows.rediscache.service.RedisService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
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
class RedisRouteTest extends CamelQuarkusTestSupport {

	EsthesisDataMessageSerializer esthesisDataMessageSerializer = new EsthesisDataMessageSerializer();

	@InjectMock
	RedisService redisService;

	@BeforeEach
	@SneakyThrows
	void setup() {
		// Prevent the actual execution of the InfluxDBService logic during the test.
		doNothing().when(redisService).process(any());

		// Log the currently registered Camel routes to assist with identifying route IDs.
		context.getRoutes().forEach(
			route -> log.info("Route id: {}, Route uri: {}", route.getId(),
				route.getEndpoint().getEndpointUri()));

		// Replace the Kafka telemetry endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "kafka-telemetry-to-redis", a -> {
			a.replaceFromWith("direct:test-telemetry");
		});

		// Replace the Kafka metadata endpoint with a direct endpoint for injecting test messages.
		AdviceWith.adviceWith(context, "kafka-metadata-to-redis", a -> {
			a.replaceFromWith("direct:test-metadata");
		});
	}

	@Test
	@SneakyThrows
	void testRoutes() {
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

		// Send the test message through the direct endpoint, simulating a Kafka event.
		template.sendBody("direct:test-telemetry", telemetryMessage);

		// Send the test message through the direct endpoint, simulating a Kafka event.
		template.sendBody("direct:test-metadata", metadataMessage);

		// Verify that each message was processed as expected.
		verify(redisService, times(2)).process(any());
	}
}
