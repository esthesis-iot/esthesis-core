package esthesis.core.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class KeyPairDeserializerTest {

	private KeyPairDeserializer deserializer;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		deserializer = new KeyPairDeserializer(KeyPair.class);
		objectMapper = new ObjectMapper();
	}

	@SneakyThrows
	@Test
	void testDeserialize() {
		// Generate an RSA key pair for testing.
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();

		// Encode the public and private keys in Base64 format.
		String publicKeyEncoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
		String privateKeyEncoded = Base64.getEncoder().encodeToString(privateKey.getEncoded());

		// Create a JSON object containing the encoded key pair.
		ObjectNode jsonNode = objectMapper.createObjectNode();
		jsonNode.put(KeyPairSerializer.PUBLIC_KEY, publicKeyEncoded);
		jsonNode.put(KeyPairSerializer.PRIVATE_KEY, privateKeyEncoded);

		// Convert the JSON object to a string representation.
		String jsonString = objectMapper.writeValueAsString(jsonNode);

		// Parse JSON string back to validate its correctness before deserialization.
		JsonNode parsedJson = objectMapper.readTree(jsonString);
		assertTrue(parsedJson.has(KeyPairSerializer.PUBLIC_KEY), "JSON should contain the public key field.");
		assertTrue(parsedJson.has(KeyPairSerializer.PRIVATE_KEY), "JSON should contain the private key field.");

		// Create a JsonParser from the JSON string.
		JsonParser jsonParser = objectMapper.createParser(jsonString);

		// Deserialize the JSON string into a KeyPair object.
		KeyPair deserializedKeyPair = deserializer.deserialize(jsonParser, objectMapper.getDeserializationContext());

		// Validate that the deserialized key pair is not null.
		assertNotNull(deserializedKeyPair);

		// Verify that the deserialized public and private keys match the original ones.
		assertArrayEquals(publicKey.getEncoded(),
			deserializedKeyPair.getPublic().getEncoded(),
			"Deserialized public key should match original.");

		assertArrayEquals(privateKey.getEncoded(),
			deserializedKeyPair.getPrivate().getEncoded(),
			"Deserialized private key should match original.");
	}
}
