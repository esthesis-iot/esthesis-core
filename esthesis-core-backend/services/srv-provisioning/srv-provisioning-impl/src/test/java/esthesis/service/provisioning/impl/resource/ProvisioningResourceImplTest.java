package esthesis.service.provisioning.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.common.gridfs.GridFSDTO;
import esthesis.service.common.gridfs.GridFSService;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.TestHelper;
import esthesis.service.provisioning.impl.service.ProvisioningService;
import esthesis.service.provisioning.resource.ProvisioningResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.core.common.AppConstants.Provisioning.Type.INTERNAL;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(ProvisioningResource.class)
class ProvisioningResourceImplTest {

	@Inject
	ProvisioningService provisioningService;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	SettingsResource settingsResource;

	@Inject
	TestHelper testHelper;

	@InjectMock
	GridFSService gridFSService;

	@SneakyThrows
	@BeforeEach
	void setUp() {
		// Clear the test database before each test.
		testHelper.clearDatabase();

		// Mock semantic versioning as enabled.
		when(settingsResource.findByName(AppConstants.NamedSetting.DEVICE_PROVISIONING_SEMVER))
			.thenReturn(new SettingEntity(AppConstants.NamedSetting.DEVICE_PROVISIONING_SEMVER.name(), "true"));

		// Mock GridFS operations save, delete and download.
		when(gridFSService.saveBinary(any(GridFSDTO.class))).thenReturn(new ObjectId());
		doNothing().when(gridFSService).deleteBinary(any(GridFSDTO.class));
		when(gridFSService.downloadBinary(any(GridFSDTO.class))).thenReturn(
			Uni.createFrom().item(new byte[0]));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void find() {
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
			.when().get("/v1/find?page=0&size=10&sort=createdOn,desc")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());


	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByIds() {
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
			.queryParam("ids", provisioningPackage.getId().toHexString())
			.when().get("/v1/find/by-ids")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
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
			.pathParam("id", provisioningPackage.getId().toHexString())
			.when().get("/v1/{id}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void save() {
		ProvisioningPackageEntity provisioningPackage =
			testHelper.createProvisioningPackageEntity(
				"test-provisioning-package",
				"1.0.1",
				"1.0.0",
				true,
				List.of("test-tag"),
				INTERNAL);

		provisioningPackage.setCreatedOn(null);

		given()
			.contentType(ContentType.MULTIPART)
			.multiPart("dto", provisioningPackage, MediaType.APPLICATION_JSON)
			.multiPart("file", "file.test", "test".getBytes(), MediaType.TEXT_PLAIN)
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void delete() {
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
			.pathParam("id", provisioningPackage.getId().toHexString())
			.when().delete("/v1/{id}")
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	@Disabled("Disabled until timed out error is being addressed.")
	void download() {
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
			.contentType(ContentType.JSON)
			.pathParam("id", provisioningPackage.getId().toHexString())
			.when().get("/v1/{id}/download")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.BINARY);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByTags() {
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
			.queryParam("tags", "test-tag")
			.when().get("/v1/find/by-tags")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}
}
