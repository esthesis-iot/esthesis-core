package esthesis.service.crypto.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.impl.TestHelper;
import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.resource.CASystemResource;
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

import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(CASystemResource.class)
class CASystemResourceImplTest {

	@Inject
	TestHelper testHelper;

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
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getCACertificate() {
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));
		String response =
			given()
				.pathParam("caId", ca.getId().toHexString())
				.when().get("/v1/{caId}/certificate")
				.then()
				.log().all()
				.statusCode(200)
				.contentType(ContentType.TEXT)
				.extract().asString();

		assertNotNull(response);
	}
}
