package esthesis.core.common.serder.camel;

import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import lombok.SneakyThrows;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EsthesisDataMessageDataFormatTest {
	private EsthesisDataMessageDataFormat dataFormat;
	private Exchange exchange;

	@BeforeEach
	void setUp() {
		dataFormat = new EsthesisDataMessageDataFormat();
		exchange = mock(Exchange.class);
	}

	@Test
	@SneakyThrows
	void testMarshal() {
		// Create a test message.
		EsthesisDataMessage message =
			new EsthesisDataMessage(
				"id123",
				"correlationId456",
				"hardwareIdTest",
				"testComponent",
				"2025-02-12T12:00:00.123Z",
				esthesis.common.avro.MessageTypeEnum.T,
				"testChannel",
				new esthesis.common.avro.PayloadData(
					"environment",
					"2025-02-12T12:00:00.123Z",
					List.of(new ValueData("temperature", "25.1", ValueTypeEnum.BIG_DECIMAL))));

		// Marshal the message to an output stream.
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		dataFormat.marshal(exchange, message, outputStream);
		byte[] marshaledData = outputStream.toByteArray();

		assertNotNull(marshaledData, "Marshalled data should not be null.");
		assertTrue(marshaledData.length > 0, "Marshalled data should not be empty.");
	}

	@Test
	@SneakyThrows
	void testUnmarshal() {
		// Create a test message and serialize it to bytes.
		EsthesisDataMessage originalMessage =
			new EsthesisDataMessage(
				"id123",
				"correlationId456",
				"hardwareIdTest",
				"testComponent",
				"2025-02-12T12:00:00.123Z",
				esthesis.common.avro.MessageTypeEnum.T,
				"testChannel",
				new esthesis.common.avro.PayloadData(
					"environment",
					"2025-02-12T12:00:00.123Z",
					List.of(new ValueData("temperature", "25.1", ValueTypeEnum.BIG_DECIMAL))));

		ByteBuffer buffer = originalMessage.toByteBuffer();

		// Unmarshal the data back into an object.
		ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer.array());
		EsthesisDataMessage deserializedMessage = (EsthesisDataMessage) dataFormat.unmarshal(exchange, inputStream);

		assertNotNull(deserializedMessage, "Deserialized message should not be null.");
		assertEquals(originalMessage.getId(), deserializedMessage.getId(), "IDs should match.");
		assertEquals(originalMessage.getCorrelationId(),
			deserializedMessage.getCorrelationId(),
			"Correlation IDs should match.");
		assertEquals(originalMessage.getHardwareId(),
			deserializedMessage.getHardwareId(),
			"Hardware IDs should match.");
		assertEquals(originalMessage.getSeenBy(), deserializedMessage.getSeenBy(), "SeenBy should match.");
		assertEquals(originalMessage.getSeenAt(), deserializedMessage.getSeenAt(), "SeenAt should match.");
		assertEquals(originalMessage.getType(), deserializedMessage.getType(), "MessageTypeEnum should match.");
		assertEquals(originalMessage.getChannel(), deserializedMessage.getChannel(), "Channel should match.");
		assertEquals(originalMessage.getPayload().getCategory(),
			deserializedMessage.getPayload().getCategory(), "Payload category should match.");
	}

}
