package esthesis.core.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MongoInstantDeserializerTest {

	private MongoInstantDeserializer deserializer;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		deserializer = new MongoInstantDeserializer();
		objectMapper = new ObjectMapper();
	}

	@Test
	@SneakyThrows
	void testDeserializeFromMongoFormat() {
		// Create a JSON object mimicking MongoDB's date format.
		ObjectNode jsonNode = objectMapper.createObjectNode();
		String instantString = "2025-02-12T12:00:00.123Z";
		jsonNode.put("$date", instantString);

		// Convert the JSON object to a string representation.
		String jsonString = objectMapper.writeValueAsString(jsonNode);
		JsonParser jsonParser = objectMapper.createParser(jsonString);

		// Deserialize the JSON into an Instant.
		Instant deserializedInstant = deserializer.deserialize(jsonParser, objectMapper.getDeserializationContext());

		// Verify that the deserialized Instant matches the expected value.
		assertEquals(
			Instant.parse(instantString),
			deserializedInstant,
			"Deserialized Instant should match expected value.");
	}

	@Test
	@SneakyThrows
	void testDeserializeFromPlainString() {
		// Create a plain string JSON representation of an Instant.
		String instantString = "2025-02-12T12:00:00.123Z";
		String jsonString = objectMapper.writeValueAsString(instantString);
		JsonParser jsonParser = objectMapper.createParser(jsonString);

		// Deserialize the JSON into an Instant.
		Instant deserializedInstant = deserializer.deserialize(jsonParser, objectMapper.getDeserializationContext());

		// Verify that the deserialized Instant matches the expected value.
		assertEquals(
			Instant.parse(instantString),
			deserializedInstant,
			"Deserialized Instant should match expected value.");
	}

}
