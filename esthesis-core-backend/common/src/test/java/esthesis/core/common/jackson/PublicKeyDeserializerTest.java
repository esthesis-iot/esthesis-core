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
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PublicKeyDeserializerTest {
	private PublicKeyDeserializer deserializer;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		deserializer = new PublicKeyDeserializer(PublicKey.class);
		objectMapper = new ObjectMapper();
	}

	@Test
	@SneakyThrows
	void testDeserialize() {
		// Generate an RSA key pair for testing.
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		PublicKey publicKey = keyPair.getPublic();

		// Encode the public key in Base64 format.
		String publicKeyEncoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		// Create a JSON object containing the encoded public key.
		ObjectNode jsonNode = objectMapper.createObjectNode();
		jsonNode.put("publicKey", publicKeyEncoded);

		// Convert the JSON object to a string representation.
		String jsonString = objectMapper.writeValueAsString(jsonNode);

		// Create a JsonParser from the JSON string.
		JsonParser jsonParser = objectMapper.createParser(jsonString);

		// Deserialize the JSON string into a PublicKey object.
		DeserializationContext deserializationContext = objectMapper.getDeserializationContext();
		PublicKey deserializedPublicKey = deserializer.deserialize(jsonParser, deserializationContext);

		// Validate that the deserialized public key is not null.
		assertNotNull(deserializedPublicKey);

		// Verify that the deserialized public key matches the original one.
		assertArrayEquals(
			publicKey.getEncoded(),
			deserializedPublicKey.getEncoded(),
			"Deserialized public key should match original.");
	}

}
