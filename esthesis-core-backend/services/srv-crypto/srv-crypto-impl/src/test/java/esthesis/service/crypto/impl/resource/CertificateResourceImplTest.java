package esthesis.service.crypto.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.impl.TestHelper;
import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.impl.service.CertificateService;
import esthesis.service.crypto.resource.CertificateResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(CertificateResource.class)
class CertificateResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Inject
	CertificateService certificateService;

	@Inject
	CAService caService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
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
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void find() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		certificateService.save(testHelper.makeCertificateEntity(ca));

		String response =
			given()
				.contentType(ContentType.TEXT)
				.when().get("/v1/find?page=0&size=10&sort=createdOn,desc")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.TEXT)
				.extract().asString();

		assertNotNull(response);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findById() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		CertificateEntity certificate = certificateService.save(testHelper.makeCertificateEntity(ca));

		String response =
			given()
				.contentType(ContentType.TEXT)
				.pathParam("id", certificate.getId().toHexString())
				.when().get("/v1/{id}")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.TEXT)
				.extract().asString();

		assertFalse(response.isEmpty());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByIdComplete() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		CertificateEntity certificate = certificateService.save(testHelper.makeCertificateEntity(ca));

		given()
			.contentType(ContentType.JSON)
			.pathParam("id", certificate.getId().toHexString())
			.when().get("/v1/{id}/complete")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void download() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		CertificateEntity certificate = certificateService.save(testHelper.makeCertificateEntity(ca));

		// Download public key.
		String responsePublicKey =
			given()
				.pathParam("id", certificate.getId().toHexString())
				.queryParam("type", AppConstants.KeyType.PUBLIC)
				.when().get("/v1/{id}/download")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.BINARY)
				.extract().asString();

		assertNotNull(responsePublicKey);

		// Download private key.
		String responsePrivateKey =
			given()
				.pathParam("id", certificate.getId().toHexString())
				.queryParam("type", AppConstants.KeyType.PRIVATE)
				.when().get("/v1/{id}/download")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.BINARY)
				.extract().asString();
		assertNotNull(responsePrivateKey);

		// Download certificate.
		String responseCertificate =
			given()
				.pathParam("id", certificate.getId().toHexString())
				.queryParam("type", AppConstants.KeyType.CERTIFICATE)
				.when().get("/v1/{id}/download")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.BINARY)
				.extract().asString();

		assertNotNull(responseCertificate);

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void importCertificate() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		CertificateEntity certificate = certificateService.save(testHelper.makeCertificateEntity(ca));

		byte[] publicKeyContent = certificate.getPublicKey().getBytes();
		byte[] privateKeyContent = certificate.getPrivateKey().getBytes();
		byte[] certificateContent = certificate.getCertificate().getBytes();

		given()
			.contentType(ContentType.MULTIPART)
			.multiPart("dto", ca, MediaType.APPLICATION_JSON)
			.multiPart("public", "publicKey.pem", publicKeyContent, MediaType.TEXT_PLAIN)
			.multiPart("private", "privateKey.pem", privateKeyContent, MediaType.TEXT_PLAIN)
			.multiPart("certificate", "certificate.pem", certificateContent, MediaType.TEXT_PLAIN)
			.when().post("/v1/import")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("id", notNullValue())
			.body("cn", equalTo(certificate.getCn()));

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void delete() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		CertificateEntity certificate = certificateService.save(testHelper.makeCertificateEntity(ca));

		given()
			.pathParam("id", certificate.getId().toHexString())
			.when().delete("/v1/{id}")
			.then()
			.log().all()
			.statusCode(204);

		assertNull(certificateService.findById(certificate.getId().toHexString()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void save() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		given()
			.contentType(ContentType.JSON)
			.body(testHelper.makeCertificateEntity(ca))
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("id", notNullValue());
	}
}
