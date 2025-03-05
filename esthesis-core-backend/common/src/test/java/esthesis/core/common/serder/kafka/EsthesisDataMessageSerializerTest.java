package esthesis.core.common.serder.kafka;

import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.apache.kafka.common.header.Headers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EsthesisDataMessageSerializerTest {

	private EsthesisDataMessageSerializer esthesisDataMessageSerializer;
	private EsthesisDataMessage esthesisDataMessage;

	@BeforeEach
	void setUp() {
		esthesisDataMessageSerializer = new EsthesisDataMessageSerializer();
		esthesisDataMessage = EsthesisDataMessage.newBuilder()
			.setId(UUID.randomUUID().toString())
			.setHardwareId("test-hardware")
			.setType(esthesis.common.avro.MessageTypeEnum.P)
			.setChannel("test-channel")
			.setSeenAt("2025-02-01T12:00:00Z")
			.setSeenBy("test-seen-by")
			.setCorrelationId("test-correlation-id")
			.setPayload(
				new esthesis.common.avro.PayloadData(
					"environment",
					"2025-02-01T12:00:00Z",
					List.of(new ValueData("temperature", "25.1", ValueTypeEnum.BIG_DECIMAL))))
			.build();
	}

	@SneakyThrows
	@Test
	void testSerializeWithTopic() {
		byte[] serialized = esthesisDataMessageSerializer.serialize("test-topic", esthesisDataMessage);
		assertNotNull(serialized);
		assertTrue(serialized.length > 0);

		EsthesisDataMessage deserialized = EsthesisDataMessage.fromByteBuffer(ByteBuffer.wrap(serialized));
		assertEquals(esthesisDataMessage.getId(), deserialized.getId());
		assertEquals(esthesisDataMessage.getHardwareId(), deserialized.getHardwareId());
		assertEquals(esthesisDataMessage.getType(), deserialized.getType());
		assertEquals(esthesisDataMessage.getChannel(), deserialized.getChannel());
		assertEquals(esthesisDataMessage.getSeenAt(), deserialized.getSeenAt());
		assertEquals(esthesisDataMessage.getSeenBy(), deserialized.getSeenBy());
		assertEquals(esthesisDataMessage.getCorrelationId(), deserialized.getCorrelationId());
		assertEquals(esthesisDataMessage.getPayload(), deserialized.getPayload());
	}

	@Test
	@SneakyThrows
	void testSerializeWithTopicAndHeaders() {
		// Mock headers.
		Headers mockHeaders = Mockito.mock(Headers.class);

		byte[] serialized = esthesisDataMessageSerializer.serialize("test-topic", mockHeaders, esthesisDataMessage);
		assertNotNull(serialized);
		assertTrue(serialized.length > 0);

		EsthesisDataMessage deserialized = EsthesisDataMessage.fromByteBuffer(ByteBuffer.wrap(serialized));
		assertEquals(esthesisDataMessage.getId(), deserialized.getId());
		assertEquals(esthesisDataMessage.getHardwareId(), deserialized.getHardwareId());
		assertEquals(esthesisDataMessage.getType(), deserialized.getType());
		assertEquals(esthesisDataMessage.getChannel(), deserialized.getChannel());
		assertEquals(esthesisDataMessage.getSeenAt(), deserialized.getSeenAt());
		assertEquals(esthesisDataMessage.getSeenBy(), deserialized.getSeenBy());
		assertEquals(esthesisDataMessage.getCorrelationId(), deserialized.getCorrelationId());
		assertEquals(esthesisDataMessage.getPayload(), deserialized.getPayload());
	}

	@Test
	void testConfigure() {
		assertDoesNotThrow(() -> esthesisDataMessageSerializer.configure(null, false));
	}

	@Test
	void testClose() {
		assertDoesNotThrow(() -> esthesisDataMessageSerializer.close());
	}

}
