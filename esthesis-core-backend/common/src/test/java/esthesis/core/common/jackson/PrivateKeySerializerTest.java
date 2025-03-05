package esthesis.core.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PrivateKeySerializerTest {

	private PrivateKeySerializer serializer;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		serializer = new PrivateKeySerializer(PrivateKey.class);
		objectMapper = new ObjectMapper();
	}

	@Test
	@SneakyThrows
	void testSerialize() {
		// Generate an RSA key pair for testing.
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		PrivateKey privateKey = keyPair.getPrivate();

		// Create a StringWriter to capture JSON output.
		StringWriter writer = new StringWriter();
		JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(writer);
		SerializerProvider serializerProvider = objectMapper.getSerializerProvider();

		// Serialize the PrivateKey object.
		serializer.serialize(privateKey, jsonGenerator, serializerProvider);
		jsonGenerator.flush();

		// Retrieve and parse the JSON output.
		JsonNode jsonNode = objectMapper.readTree(writer.toString());

		// Ensure the expected field exists.
		assertTrue(
			jsonNode.has(PrivateKeySerializer.PRIVATE_KEY),
			"JSON output should contain the private key field.");

		// Verify that the Base64-encoded private key matches the original.
		String expectedPrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		assertEquals(
			expectedPrivateKey,
			Base64.getEncoder().encodeToString(jsonNode.get(PrivateKeySerializer.PRIVATE_KEY).binaryValue()),
			"Serialized private key should match original.");
	}

}
