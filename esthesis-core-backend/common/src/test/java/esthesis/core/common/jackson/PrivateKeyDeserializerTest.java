package esthesis.core.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PrivateKeyDeserializerTest {
	private PrivateKeyDeserializer deserializer;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		deserializer = new PrivateKeyDeserializer(PrivateKey.class);
		objectMapper = new ObjectMapper();
	}

	@Test
	@SneakyThrows
	void testDeserialize() {
		// Generate an RSA key pair for testing.
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		PrivateKey privateKey = keyPair.getPrivate();

		// Encode the private key in Base64 format.
		String privateKeyEncoded = Base64.getEncoder().encodeToString(privateKey.getEncoded());

		// Create a JSON object containing the encoded private key.
		ObjectNode jsonNode = objectMapper.createObjectNode();
		jsonNode.put("privateKey", privateKeyEncoded);

		// Convert the JSON object to a string representation.
		String jsonString = objectMapper.writeValueAsString(jsonNode);

		// Create a JsonParser from the JSON string.
		JsonParser jsonParser = objectMapper.createParser(jsonString);

		// Deserialize the JSON string into a PrivateKey object.
		DeserializationContext deserializationContext = objectMapper.getDeserializationContext();
		PrivateKey deserializedPrivateKey = deserializer.deserialize(jsonParser, deserializationContext);

		// Validate that the deserialized private key is not null.
		assertNotNull(deserializedPrivateKey);

		// Verify that the deserialized private key matches the original one.
		assertArrayEquals(
			privateKey.getEncoded(),
			deserializedPrivateKey.getEncoded(),
			"Deserialized private key should match original.");
	}

}
