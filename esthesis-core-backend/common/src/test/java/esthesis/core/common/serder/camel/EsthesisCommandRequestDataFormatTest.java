package esthesis.core.common.serder.camel;

import esthesis.common.avro.EsthesisCommandRequestMessage;
import lombok.SneakyThrows;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EsthesisCommandRequestDataFormatTest {
	private EsthesisCommandRequestDataFormat dataFormat;
	private Exchange exchange;

	@BeforeEach
	void setUp() {
		dataFormat = new EsthesisCommandRequestDataFormat();
		exchange = mock(Exchange.class);
	}

	@Test
	@SneakyThrows
	void testMarshal() {
		// Create a test message.
		EsthesisCommandRequestMessage message =
			new EsthesisCommandRequestMessage(
				"id123",
				"hardwareId123",
				esthesis.common.avro.CommandType.e,
				esthesis.common.avro.ExecutionType.a,
				"executeCommand",
				"arg1,arg2",
				"2025-02-12T12:00:00.123Z");

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
		EsthesisCommandRequestMessage originalMessage =
			new EsthesisCommandRequestMessage(
				"id123",
				"hardwareId123",
				esthesis.common.avro.CommandType.e,
				esthesis.common.avro.ExecutionType.a,
				"executeCommand",
				"arg1,arg2",
				"2025-02-12T12:00:00.123Z");

		ByteBuffer buffer = originalMessage.toByteBuffer();

		// Unmarshal the data back into an object.
		ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer.array());
		EsthesisCommandRequestMessage deserializedMessage = (EsthesisCommandRequestMessage) dataFormat.unmarshal(exchange, inputStream);

		assertNotNull(deserializedMessage, "Deserialized message should not be null.");
		assertEquals(originalMessage.getId(), deserializedMessage.getId(), "IDs should match.");
		assertEquals(originalMessage.getHardwareId(), deserializedMessage.getHardwareId(), "Hardware IDs should match.");
		assertEquals(originalMessage.getCommandType(), deserializedMessage.getCommandType(), "CommandType should match.");
		assertEquals(originalMessage.getExecutionType(), deserializedMessage.getExecutionType(), "ExecutionType should match.");
		assertEquals(originalMessage.getCommand(), deserializedMessage.getCommand(), "Command should match.");
		assertEquals(originalMessage.getArguments(), deserializedMessage.getArguments(), "Arguments should match.");
		assertEquals(originalMessage.getCreatedAt(), deserializedMessage.getCreatedAt(), "CreatedAt should match.");
	}

}
