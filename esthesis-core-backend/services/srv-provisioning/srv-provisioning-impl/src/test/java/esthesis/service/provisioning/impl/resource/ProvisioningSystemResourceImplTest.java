package esthesis.service.provisioning.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.TestHelper;
import esthesis.service.provisioning.impl.service.ProvisioningService;
import esthesis.service.provisioning.resource.ProvisioningSystemResource;
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

import static esthesis.core.common.AppConstants.Provisioning.Type.INTERNAL;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(ProvisioningSystemResource.class)
class ProvisioningSystemResourceImplTest {

	@Inject
	ProvisioningService provisioningService;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	SettingsResource settingsResource;


	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	DeviceResource deviceResource;

	@Inject
	TestHelper testHelper;


	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Mock semantic versioning as enabled.
		when(settingsResource.findByName(AppConstants.NamedSetting.DEVICE_PROVISIONING_SEMVER))
			.thenReturn(new SettingEntity(AppConstants.NamedSetting.DEVICE_PROVISIONING_SEMVER.name(), "true"));

		// Mock the device resource to return a device entity with the specified hardware ID and tags.
		when(deviceResource.findByHardwareIds(anyString())).thenReturn(List.of(
			testHelper.createDeviceEntity("test-hardware-id", List.of("test-tag"))));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void find() {

		provisioningService.saveNew(testHelper.createProvisioningPackageEntity(
			"test-provisioning-package",
			"2.1.1",
			null,
			true,
			List.of("test-tag"),
			INTERNAL), testHelper.createFileUpload());

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.queryParam("version", "1.0.0")
			.when().get("/v1/system/find/{hardwareId}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void findById() {
		ProvisioningPackageEntity provisioningPackage =
			provisioningService.saveNew(
				testHelper.createProvisioningPackageEntity(
					"test-provisioning-package",
					"1.0.1",
					"1.0.0",
					true,
					List.of("test-tag"),
					INTERNAL),
				testHelper.createFileUpload());

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.pathParam("provisioningPackageId", provisioningPackage.getId().toHexString())
			.when().get("/v1/system/find/by-id/{provisioningPackageId}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}
}
