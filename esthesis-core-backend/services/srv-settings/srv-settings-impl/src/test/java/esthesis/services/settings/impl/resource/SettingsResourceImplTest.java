package esthesis.services.settings.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.services.settings.impl.TestHelper;
import esthesis.services.settings.impl.service.SettingsService;
import esthesis.util.redis.RedisUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(SettingsResource.class)
class SettingsResourceImplTest {

	@Inject
	SettingsService settingsService;

	@Inject
	TestHelper testHelper;

	@InjectMock
	RedisUtils redisUtils;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Mock retrieving measurements from redis.
		when(redisUtils.findKeysStartingWith(RedisUtils.KeyType.ESTHESIS_DM.toString()))
			.thenReturn(List.of("category.measurement1", "category.measurement2", "category.measurement3"));
		when(redisUtils.getHash("category.measurement1"))
			.thenReturn(Map.of("category.measurement1", "test-value1"));
		when(redisUtils.getHash("category.measurement2"))
			.thenReturn(Map.of("category.measurement2", "test-value2"));
		when(redisUtils.getHash("category.measurement3"))
			.thenReturn(Map.of("category.measurement3", "test-value3"));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByName() {
		settingsService.saveNew(new SettingEntity(NamedSetting.DEVICE_PUSHED_TAGS.name(), "test-value"));


		given()
			.accept(ContentType.JSON)
			.pathParam("name", NamedSetting.DEVICE_PUSHED_TAGS.name())
			.when().get("/v1/find/by-name/{name}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByNames() {
		settingsService.saveNew(new SettingEntity(NamedSetting.DEVICE_PUSHED_TAGS.name(), "test-value"));

		given()
			.accept(ContentType.JSON)
			.pathParam("names", NamedSetting.DEVICE_PUSHED_TAGS.name())
			.when().get("/v1/find/by-names/{names}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void save() {

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(List.of(new SettingEntity(NamedSetting.DEVICE_PUSHED_TAGS.name(), "test-value"), new SettingEntity("test-setting", "test-value")))
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findAllUniqueMeasurementNames() {
		given()
			.accept(ContentType.JSON)
			.when().get("/v1/find-measurement-names")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getDevicePageFields() {
		given()
			.accept(ContentType.JSON)
			.when().get("/v1/device-page-fields")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void saveDevicePageFields() {
		given()
			.contentType(ContentType.JSON)
			.body(List.of(testHelper.makeDevicePageFieldEntity("test-device-page-field")))
			.when().post("/v1/device-page-fields")
			.then()
			.log().all()
			.statusCode(204);
	}
}
