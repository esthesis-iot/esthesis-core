package esthesis.services.settings.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.services.settings.impl.TestHelper;
import esthesis.services.settings.impl.service.SettingsService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(SettingsSystemResourceImpl.class)
class SettingsSystemResourceImplTest {

	@Inject
	SettingsService settingsService;

	@Inject
	TestHelper testHelper;


	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void findByName() {
		settingsService.saveNew(new SettingEntity(AppConstants.NamedSetting.DEVICE_PUSHED_TAGS.name(), "test-value"));


		given()
			.accept(ContentType.JSON)
			.pathParam("name", AppConstants.NamedSetting.DEVICE_PUSHED_TAGS.name())
			.when().get("/v1/system/find/by-name/{name}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}
}
