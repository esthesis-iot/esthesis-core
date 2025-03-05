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
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class KeyPairSerializerTest {

	private KeyPairSerializer serializer;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		serializer = new KeyPairSerializer(KeyPair.class);
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
		PrivateKey privateKey = keyPair.getPrivate();

		// Create a StringWriter to capture JSON output.
		StringWriter writer = new StringWriter();
		JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(writer);
		SerializerProvider serializerProvider = objectMapper.getSerializerProvider();

		// Serialize the KeyPair object.
		serializer.serialize(keyPair, jsonGenerator, serializerProvider);
		jsonGenerator.flush();

		// Retrieve and parse the JSON output.
		JsonNode jsonNode = objectMapper.readTree(writer.toString());

		// Ensure the expected fields exist.
		assertTrue(jsonNode.has(KeyPairSerializer.PUBLIC_KEY), "JSON output should contain the public key field.");
		assertTrue(jsonNode.has(KeyPairSerializer.PRIVATE_KEY), "JSON output should contain the private key field.");

		// Verify that the Base64-encoded keys match the original.
		String expectedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
		String expectedPrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());

		assertEquals(expectedPublicKey,
			Base64.getEncoder().encodeToString(jsonNode.get(KeyPairSerializer.PUBLIC_KEY)
				.binaryValue()),
			"Public key does not match.");

		assertEquals(expectedPrivateKey,
			Base64.getEncoder()
				.encodeToString(
					jsonNode.get(KeyPairSerializer.PRIVATE_KEY)
						.binaryValue()), "Private key does not match.");
	}

}
