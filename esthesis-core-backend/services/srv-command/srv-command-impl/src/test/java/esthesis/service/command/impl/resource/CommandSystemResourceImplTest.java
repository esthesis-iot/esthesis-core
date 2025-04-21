package esthesis.service.command.impl.resource;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.core.common.AppConstants;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.TestHelper;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.resource.TagResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(CommandSystemResource.class)
class CommandSystemResourceImplTest {

	@Inject
	TestHelper testHelper;

	@InjectMock
	@RestClient
	SettingsResource settingsResource;

	@InjectMock
	@RestClient
	DeviceResource deviceResource;

	@InjectMock
	@RestClient
	TagResource tagResource;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		when(settingsResource.findByName(any(AppConstants.NamedSetting.class))).thenReturn(mock(SettingEntity.class));
		when(deviceResource.findByHardwareIds(anyString())).thenReturn(List.of(testHelper.mockDeviceEntity("test-hardwareId")));
		when(tagResource.findByIds(anyString())).thenReturn(List.of(testHelper.mockTagEntity("tag1")));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void save() {

		CommandRequestEntity commandRequest =
			testHelper.makeCommandRequestEntity(
				"test-hardwareId",
				"tag1",
				CommandType.e,
				ExecutionType.a);

		given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(commandRequest)
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(not(emptyOrNullString()));


	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getReplies() {
		String correlationId =
			testHelper.createCommandReplyEntity(
					"test-hardwareId",
					"testCorrelationId",
					"test output",
					true)
				.getCorrelationId();

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/reply/" + correlationId)
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", is(1));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void countCollectedReplies() {
		String correlationId =
			testHelper.createCommandReplyEntity(
					"test-hardwareId",
					"testCorrelationId",
					"test output",
					true)
				.getCorrelationId();

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/reply/count/" + correlationId)
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("$", is(1));
	}
}
