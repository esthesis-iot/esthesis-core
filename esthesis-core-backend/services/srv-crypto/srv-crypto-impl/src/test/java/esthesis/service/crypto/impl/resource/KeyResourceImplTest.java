package esthesis.service.crypto.impl.resource;

import esthesis.common.crypto.dto.CreateCertificateRequestDTO;
import esthesis.core.common.AppConstants;
import esthesis.service.crypto.impl.TestHelper;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(KeyResource.class)
class KeyResourceImplTest {

	@Inject
	TestHelper testHelper;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	SettingsResource settingsResource;


	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Mock the security-related settings.
		SettingEntity mockKeyAlgorithmSetting = mock(SettingEntity.class);
		SettingEntity mockKeySizeSetting = mock(SettingEntity.class);
		SettingEntity mockSignatureAlgorithmSetting = mock(SettingEntity.class);

		when(mockKeyAlgorithmSetting.asString()).thenReturn("RSA");
		when(mockKeySizeSetting.asInt()).thenReturn(2048);
		when(mockSignatureAlgorithmSetting.asString()).thenReturn("SHA256withRSA");

		when(settingsResource.findByName(SECURITY_ASYMMETRIC_KEY_ALGORITHM))
			.thenReturn(mockKeyAlgorithmSetting);
		when(settingsResource.findByName(SECURITY_ASYMMETRIC_KEY_SIZE))
			.thenReturn(mockKeySizeSetting);
		when(settingsResource.findByName(SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM))
			.thenReturn(mockSignatureAlgorithmSetting);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void generateKeyPair() {
		given()
			.when().get("/v1/keypair")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("publicKey", notNullValue())
			.body("privateKey", notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	@Disabled("Disabled until the issue generating a valid public key param is resolved")
	void publicKeyToPEM() {

		given()
			.contentType(ContentType.JSON)
			.queryParam("publicKey", "publicKey") // Todo find way to generate a valid public key param.
			.when().get("/v1/publicKeyToPEM")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.TEXT);
	}

	@Test
	@Disabled("Disabled until the issue generating a valid private key param is resolved")
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void privateKeyToPEM() {

		given()
			.queryParam("privateKey", "privateKey") // Todo find way to generate a valid private key param.
			.when().get("/v1/privateKeyToPEM")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.TEXT);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	@Disabled("Disabled until the issue generating a valid request param is resolved")
	void generateCertificateAsPEM() {
		given()
			.queryParam("createCertificateRequestDTO", new CreateCertificateRequestDTO()) // Todo find way to generate a valid request param.
			.when().get("/v1/certificate")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.TEXT);
	}



}
