package esthesis.dataflows.commandreplyupdater.service;

import esthesis.common.avro.EsthesisCommandReplyMessage;
import esthesis.common.avro.ReplyType;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandReplyUpdaterServiceTest {

	CommandReplyUpdaterService commandReplyUpdaterService;

	@Mock
	private Exchange exchange;

	@Mock
	private Message message;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		commandReplyUpdaterService = new CommandReplyUpdaterService();
		when(exchange.getIn()).thenReturn(message);
	}

	@Test
	void testCreateMongoEntity() {
		// Mock input EsthesisCommandReplyMessage.
		EsthesisCommandReplyMessage esthesisCommandReplyMessage = new EsthesisCommandReplyMessage(
			"test-id",
			"correlation-id",
			"hardwareTest",
			"component",
			Instant.now().toString(),
			ReplyType.s, // Success type
			"channel",
			"test payload"
		);

		when(message.getBody(EsthesisCommandReplyMessage.class)).thenReturn(esthesisCommandReplyMessage);

		// Execute method.
		commandReplyUpdaterService.createMongoEntity(exchange);

		// Capture and verify the output CommandReplyEntity.
		verify(message).setBody(argThat((doc -> {
			Document body = (Document) doc;

			System.out.println(body);

			assertEquals(esthesisCommandReplyMessage.getCorrelationId(), body.getString("correlationId"));
			assertEquals(esthesisCommandReplyMessage.getHardwareId(), body.getString("hardwareId"));
			assertEquals(esthesisCommandReplyMessage.getPayload(), body.getString("output"));
			assertNotNull(body.get("createdOn"));
			assertTrue(body.getBoolean("success"));
			assertFalse(body.getBoolean("trimmed"));
			return true;
		})));
	}

}
