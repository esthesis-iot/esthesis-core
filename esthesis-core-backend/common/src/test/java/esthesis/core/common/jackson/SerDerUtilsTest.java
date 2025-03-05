package esthesis.core.common.jackson;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SerDerUtilsTest {

	@Test
	@SneakyThrows
	void testGetPublicKey() {
		// Generate an RSA key pair for testing.
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		PublicKey originalPublicKey = keyPair.getPublic();

		// Encode the public key in Base64 format.
		String publicKeyEncoded = Base64.getEncoder().encodeToString(originalPublicKey.getEncoded());

		// Deserialize the public key from Base64.
		PublicKey deserializedPublicKey = SerDerUtils.getPublicKey(publicKeyEncoded);

		// Validate that the deserialized public key is not null and matches the original.
		assertNotNull(deserializedPublicKey);
		assertArrayEquals(
			originalPublicKey.getEncoded(),
			deserializedPublicKey.getEncoded(),
			"Deserialized public key should match original.");
	}

	@Test
	@SneakyThrows
	void testGetPrivateKey() {
		// Generate an RSA key pair for testing.
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		PrivateKey originalPrivateKey = keyPair.getPrivate();

		// Encode the private key in Base64 format.
		String privateKeyEncoded = Base64.getEncoder().encodeToString(originalPrivateKey.getEncoded());

		// Deserialize the private key from Base64.
		PrivateKey deserializedPrivateKey = SerDerUtils.getPrivateKey(privateKeyEncoded);

		// Validate that the deserialized private key is not null and matches the original.
		assertNotNull(deserializedPrivateKey);
		assertArrayEquals(
			originalPrivateKey.getEncoded(),
			deserializedPrivateKey.getEncoded(),
			"Deserialized private key should match original.");
	}

}
