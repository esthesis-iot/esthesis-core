package esthesis.service.crypto.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.impl.TestHelper;
import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.resource.CAResource;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(CAResource.class)
class CAResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Inject
	CAService caService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsResource settingsResource;

	@BeforeEach
	void clearDatabase() {
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
		caService.save(testHelper.makeCaEntity(null));

		String response =
			given()
				.contentType(ContentType.TEXT)
				.when().get("/v1/find?page=0&size=10&sort=createdOn,desc")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.TEXT)
				.extract().asString();

		assertFalse(response.isEmpty());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByIds() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		given()
			.contentType(ContentType.JSON)
			.queryParam("ids", ca.getId().toHexString())
			.when().get("/v1/find/by-ids")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", equalTo(1));


	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByCn() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		String response =
			given()
				.contentType(ContentType.TEXT)
				.queryParam("cn", ca.getCn())
				.when().get("/v1/find/by-cn")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.TEXT)
				.extract().asString();

		assertFalse(response.isEmpty());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findById() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		String response =
			given()
				.contentType(ContentType.TEXT)
				.pathParam("id", ca.getId().toHexString())
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

		given()
			.contentType(ContentType.JSON)
			.pathParam("id", ca.getId().toHexString())
			.when().get("/v1/{id}/complete")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("id", equalTo(ca.getId().toHexString()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getEligbleForSigning() {
		caService.save(testHelper.makeCaEntity(null));

		given()
			.contentType(ContentType.JSON)
			.when().get("/v1/eligible-for-signing")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", equalTo(1));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void download() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		// Download public key.
		String responsePublicKey =
			given()
				.pathParam("id", ca.getId().toHexString())
				.queryParam("type", AppConstants.KeyType.PUBLIC)
				.when().get("/v1/{id}/download")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.BINARY)
				.extract().asString();

		assertFalse(responsePublicKey.isEmpty());

		// Download private key.
		String responsePrivateKey =
			given()
				.pathParam("id", ca.getId().toHexString())
				.queryParam("type", AppConstants.KeyType.PRIVATE)
				.when().get("/v1/{id}/download")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.BINARY)
				.extract().asString();
		assertFalse(responsePrivateKey.isEmpty());

		// Download certificate.
		String responseCertificate =
			given()
				.pathParam("id", ca.getId().toHexString())
				.queryParam("type", AppConstants.KeyType.CERTIFICATE)
				.when().get("/v1/{id}/download")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.BINARY)
				.extract().asString();

		assertFalse(responseCertificate.isEmpty());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void importCa() {
		CaEntity ca = testHelper.makeCaEntity(null);

		byte[] publicKeyContent = ca.getPublicKey().getBytes();
		byte[] privateKeyContent = ca.getPrivateKey().getBytes();
		byte[] certificateContent = ca.getCertificate().getBytes();

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
			.body("name", equalTo(ca.getName()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void delete() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		given()
			.pathParam("id", ca.getId().toHexString())
			.when().delete("/v1/{id}")
			.then()
			.log().all()
			.statusCode(204);

		assertNull(caService.findById(ca.getId().toHexString()));

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void save() {

		given()
			.contentType(ContentType.JSON)
			.body(testHelper.makeCaEntity(null))
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("id", notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getCACertificate() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		String response =
			given()
				.contentType(ContentType.JSON)
				.pathParam("caId", ca.getId().toHexString())
				.when().get("/v1/{caId}/certificate")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.TEXT)
				.extract().asString();

		assertFalse(response.isEmpty());
	}
}
