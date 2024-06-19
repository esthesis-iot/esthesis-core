package esthesis.avro.util.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import esthesis.avro.CommandType;
import esthesis.avro.EsthesisCommandRequestMessage;
import esthesis.avro.ExecutionType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EsthesisCommandRequestKafkaSerializerTest {

	@Test
	void serializeTest() throws IOException {
		EsthesisCommandRequestMessage msg = new EsthesisCommandRequestMessage();
		msg.setArguments("arguments");
		msg.setCommand("command");
		msg.setHardwareId("hardwareId");
		msg.setId("id");
		msg.setExecutionType(ExecutionType.a);
		msg.setCommandType(CommandType.f);
		msg.setCreatedAt(Instant.now().toString());

		// Serialize the message.
		EsthesisCommandRequestSerializer serializer = new EsthesisCommandRequestSerializer();
		byte[] sermsg = serializer.serialize("topic", msg);
		Assertions.assertNotNull(sermsg);
		Assertions.assertTrue(sermsg.length > 0);
		System.out.println(new String(sermsg, StandardCharsets.UTF_8));

		// Deserialize the message.
		EsthesisCommandRequestMessage deserializedMsg = EsthesisCommandRequestMessage.getDecoder().decode(sermsg);

		// Assertions.
		assertEquals(msg.getArguments(), deserializedMsg.getArguments());
		assertEquals(msg.getCommand(), deserializedMsg.getCommand());
		assertEquals(msg.getHardwareId(), deserializedMsg.getHardwareId());
		assertEquals(msg.getId(), deserializedMsg.getId());
		assertEquals(msg.getExecutionType(), deserializedMsg.getExecutionType());
		assertEquals(msg.getCommandType(), deserializedMsg.getCommandType());
		assertEquals(msg.getCreatedAt(), deserializedMsg.getCreatedAt());
		assertEquals(msg, deserializedMsg);
	}
}
