package esthesis.service.crypto.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.entity.KeystoreEntity;
import esthesis.service.crypto.impl.TestHelper;
import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.impl.service.CertificateService;
import esthesis.service.crypto.impl.service.KeystoreService;
import esthesis.service.crypto.resource.KeystoreResource;
import esthesis.service.device.resource.DeviceResource;
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
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(KeystoreResource.class)
class KeystoreResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Inject
	KeystoreService keystoreService;

	@Inject
	CertificateService certificateService;

	@Inject
	CAService caService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsResource settingsResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	DeviceResource deviceResource;

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

		// Mock getting and finding devices by tag name.
		when(deviceResource.get(anyString())).thenReturn(testHelper.makeDeviceEntity("test-device-1"));
		when(deviceResource.findByTagName(anyString())).thenReturn(
			List.of(
				testHelper.makeDeviceEntity("test-device-1"),
				testHelper.makeDeviceEntity("test-device-2"))
		);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void find() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		CertificateEntity certificate = certificateService.save(testHelper.makeCertificateEntity(ca));
		keystoreService.saveNew(testHelper.makeKeystoreEntity(certificate.getId().toHexString(), ca.getId().toHexString()));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/find?page=0&size=10&sort=createdOn,desc")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.isEmpty()", equalTo(false));

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findById() {

		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		CertificateEntity certificate = certificateService.save(testHelper.makeCertificateEntity(ca));
		KeystoreEntity keystore =
			keystoreService.saveNew(testHelper.makeKeystoreEntity(certificate.getId().toHexString(), ca.getId().toHexString()));

		given()
			.accept(ContentType.JSON)
			.pathParam("id", keystore.getId().toHexString())
			.when().get("/v1/{id}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("name", equalTo(keystore.getName()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void save() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		CertificateEntity certificate = certificateService.save(testHelper.makeCertificateEntity(ca));

		KeystoreEntity keystore =
			testHelper.makeKeystoreEntity(certificate.getId().toHexString(), ca.getId().toHexString());

		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(keystore)
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("id", notNullValue());

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void delete() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		CertificateEntity certificate = certificateService.save(testHelper.makeCertificateEntity(ca));
		KeystoreEntity keystore =
			keystoreService.saveNew(testHelper.makeKeystoreEntity(certificate.getId().toHexString(), ca.getId().toHexString()));

		given()
			.pathParam("id", keystore.getId().toHexString())
			.when().delete("/v1/{id}")
			.then()
			.log().all()
			.statusCode(204);

		assertNull(keystoreService.findById(keystore.getId().toHexString()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void download() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		CertificateEntity certificate = certificateService.save(testHelper.makeCertificateEntity(ca));
		KeystoreEntity keystore =
			keystoreService.saveNew(testHelper.makeKeystoreEntity(certificate.getId().toHexString(), ca.getId().toHexString()));

		given()
			.pathParam("id", keystore.getId().toHexString())
			.when().get("/v1/{id}/download")
			.then()
			.statusCode(200)
			.contentType(ContentType.BINARY);

	}
}
