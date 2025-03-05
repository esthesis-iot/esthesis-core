package esthesis.core.common.serder.camel;

import esthesis.common.avro.EsthesisCommandReplyMessage;
import lombok.SneakyThrows;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EsthesisCommandReplyDataFormatTest {

	private EsthesisCommandReplyDataFormat esthesisCommandReplyDataFormat;
	private Exchange exchange;

	@BeforeEach
	void setUp() {
		esthesisCommandReplyDataFormat = new EsthesisCommandReplyDataFormat();
		exchange = mock(Exchange.class);
	}

	@Test
	@SneakyThrows
	void testMarshal() {
		// Create a test message.
		EsthesisCommandReplyMessage message =
			new EsthesisCommandReplyMessage(
				"id123",
				"correlationId123",
				"testHardwareId",
				"seenByComponent",
				"2025-02-12T12:00:00.123Z",
				esthesis.common.avro.ReplyType.s,
				"channel1",
				"payloadData");

		// Marshal the message to an output stream.
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		esthesisCommandReplyDataFormat.marshal(exchange, message, outputStream);
		byte[] marshaledData = outputStream.toByteArray();

		assertNotNull(marshaledData, "Marshalled data should not be null.");
		assertTrue(marshaledData.length > 0, "Marshalled data should not be empty.");
	}

	@Test
	@SneakyThrows
	void testUnmarshal() {
		// Create a test message and serialize it to bytes.
		EsthesisCommandReplyMessage originalMessage =
			new EsthesisCommandReplyMessage(
				"id123",
				"correlationId123",
				"testHardwareId",
				"seenByComponent",
				"2025-02-12T12:00:00.123Z",
				esthesis.common.avro.ReplyType.s,
				"channel1",
				"payloadData");

		ByteBuffer buffer = originalMessage.toByteBuffer();

		// Unmarshal the data back into an object.
		ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer.array());
		EsthesisCommandReplyMessage deserializedMessage =
			(EsthesisCommandReplyMessage) esthesisCommandReplyDataFormat.unmarshal(exchange, inputStream);

		assertNotNull(deserializedMessage, "Deserialized message should not be null.");
		assertEquals(originalMessage.getId(), deserializedMessage.getId(), "IDs should match.");
		assertEquals(originalMessage.getCorrelationId(),
			deserializedMessage.getCorrelationId(), "Correlation IDs should match.");
		assertEquals(originalMessage.getHardwareId(),
			deserializedMessage.getHardwareId(), "Hardware IDs should match.");
		assertEquals(originalMessage.getSeenBy(), deserializedMessage.getSeenBy(), "SeenBy should match.");
		assertEquals(originalMessage.getSeenAt(), deserializedMessage.getSeenAt(), "SeenAt should match.");
		assertEquals(originalMessage.getType(), deserializedMessage.getType(), "ReplyType should match.");
		assertEquals(originalMessage.getChannel(), deserializedMessage.getChannel(), "Channel should match.");
		assertEquals(originalMessage.getPayload(), deserializedMessage.getPayload(), "Payload should match.");
	}

	@Test
	void testCreate() {
		assertNotNull(EsthesisCommandReplyDataFormat.create(), "Created instance should not be null.");
	}

}
