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
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PublicKeySerializerTest {

	private PublicKeySerializer serializer;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		serializer = new PublicKeySerializer(PublicKey.class);
		objectMapper = new ObjectMapper();
	}

	@Test
	@SneakyThrows
	void testSerialize() {
		// Generate an RSA key pair for testing.
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		PublicKey publicKey = keyPair.getPublic();

		// Create a StringWriter to capture JSON output.
		StringWriter writer = new StringWriter();
		JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(writer);
		SerializerProvider serializerProvider = objectMapper.getSerializerProvider();

		// Serialize the PublicKey object.
		serializer.serialize(publicKey, jsonGenerator, serializerProvider);
		jsonGenerator.flush();

		// Retrieve and parse the JSON output.
		JsonNode jsonNode = objectMapper.readTree(writer.toString());

		// Ensure the expected field exists.
		assertTrue(
			jsonNode.has(PublicKeySerializer.PUBLIC_KEY),
			"JSON output should contain the public key field.");

		// Verify that the Base64-encoded public key matches the original.
		String expectedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
		assertEquals(
			expectedPublicKey,
			Base64.getEncoder().encodeToString(jsonNode.get(PublicKeySerializer.PUBLIC_KEY).binaryValue()),
			"Serialized public key should match original.");
	}

}
