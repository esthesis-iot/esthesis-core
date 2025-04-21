package esthesis.service.crypto.impl.resource;

import esthesis.common.crypto.CryptoUtil;
import esthesis.common.crypto.dto.CreateKeyPairRequestDTO;
import esthesis.common.crypto.dto.SignatureVerificationRequestDTO;
import esthesis.core.common.AppConstants;
import esthesis.service.crypto.impl.TestHelper;
import esthesis.service.crypto.resource.SigningSystemResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.Signature;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestHTTPEndpoint(SigningSystemResource.class)
class SigningSystemResourceImplTest {

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@SneakyThrows
	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void verifySignature() {

		// Create a valid RSA key pair.
		CreateKeyPairRequestDTO createKeyPairRequestDTO = new CreateKeyPairRequestDTO();
		createKeyPairRequestDTO.setKeySize(2048);
		createKeyPairRequestDTO.setKeyPairGeneratorAlgorithm("RSA");

		KeyPair keyPair = CryptoUtil.createKeyPair(createKeyPairRequestDTO);

		// Create a valid signature using the private key and the payload.
		String signatureAlgorithm = "SHA256withRSA";
		byte[] payload = "test-payload".getBytes();

		Signature signer = Signature.getInstance(signatureAlgorithm);
		signer.initSign(keyPair.getPrivate());
		signer.update(payload);

		// Sign the payload.
		byte[] signatureBytes = signer.sign();
		String base64Signature = Base64.getEncoder().encodeToString(signatureBytes);

		// Convert the public key to PEM format.
		StringWriter writer = new StringWriter();
		try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
			pemWriter.writeObject(keyPair.getPublic());
		}
		String pemPublicKey = writer.toString();


		// Build the request object.
		SignatureVerificationRequestDTO request = new SignatureVerificationRequestDTO();
		request.setSignature(base64Signature);
		request.setSignatureAlgorithm(signatureAlgorithm);
		request.setKeyAlgorithm(createKeyPairRequestDTO.getKeyPairGeneratorAlgorithm());
		request.setPublicKey(pemPublicKey);
		request.setPayload(payload);


		// Send the request to the endpoint.
		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(request)
			.when().post("/v1/verify-signature")
			.then()
			.log().all()
			.statusCode(200)
			.body("$", equalTo(true));

	}
}
