package esthesis.service.command.impl.resource;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.TestHelper;
import esthesis.service.command.impl.service.CommandService;
import esthesis.service.command.resource.CommandResource;
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
@TestHTTPEndpoint(CommandResource.class)
class CommandResourceImplTest {


	@InjectMock
	@RestClient
	SettingsResource settingsResource;

	@InjectMock
	@RestClient
	DeviceResource deviceResource;

	@InjectMock
	@RestClient
	TagResource tagResource;

	@Inject
	CommandService commandService;


	@Inject
	TestHelper testHelper;


	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		when(settingsResource.findByName(any(NamedSetting.class))).thenReturn(mock(SettingEntity.class));
		when(deviceResource.findByHardwareIds(anyString())).thenReturn(List.of(testHelper.mockDeviceEntity("test-hardwareId")));
		when(tagResource.findByIds(anyString())).thenReturn(List.of(testHelper.mockTagEntity("tag1")));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void find() {

		commandService.saveRequest(
			testHelper.makeCommandRequestEntity(
				"test-hardwareId",
				"tag1",
				CommandType.e,
				ExecutionType.a));

		given()
			.contentType(ContentType.JSON)
			.when()
			.get("/v1/find?page=0&size=10")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", is(1));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getCommand() {

		String commandId = commandService.saveRequest(
				testHelper.makeCommandRequestEntity(
						"test-hardwareId",
						"tag1",
						CommandType.e,
						ExecutionType.a)
					.setCommand("test"))
			.toHexString();

		given()
			.contentType(ContentType.JSON)
			.when()
			.get("/v1/" + commandId)
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("command", is("test"));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getReply() {

		String correlationId =
			testHelper.createCommandReplyEntity(
					"test-hardwareId",
					"testCorrelationId",
					"test output",
					true)
				.getCorrelationId();

		given()
			.contentType(ContentType.JSON)
			.when()
			.get("/v1/reply/" + correlationId)
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", is(1));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void save() {

		CommandRequestEntity commandRequest =
			testHelper.makeCommandRequestEntity(
				"test-hardwareId",
				"tag1",
				CommandType.e,
				ExecutionType.a);

		given()
			.contentType(ContentType.JSON)
			.body(commandRequest)
			.when()
			.post("/v1")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.TEXT)
			.body(not(emptyOrNullString()));

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void saveAndWait() {
		CommandRequestEntity commandRequest =
			testHelper.makeCommandRequestEntity(
				"test-hardwareId",
				"tag1",
				CommandType.e,
				ExecutionType.a);

		given()
			.contentType(ContentType.JSON)
			.body(commandRequest)
			.when()
			.post("/v1/wait-for-reply?timeout=3000&pollInterval=500")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findDevicesByHardwareId() {

		commandService.saveRequest(
			testHelper.makeCommandRequestEntity(
					"test-hardwareId",
					"tag1",
					CommandType.e,
					ExecutionType.a)
				.setCommand("test"));

		given()
			.contentType(ContentType.JSON)
			.when()
			.get("/v1/find-devices/by-hardware-id?hardwareId=test-hardwareId")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", is(1));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void deleteCommand() {
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
							"test-hardwareId",
							"tag1",
							CommandType.e,
							ExecutionType.a)
						.setCommand("test"))
				.toHexString();

		given()
			.contentType(ContentType.JSON)
			.when()
			.delete("/v1/" + commandId)
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void deleteReply() {
		String replyId =
			testHelper.createCommandReplyEntity(
					"test-hardwareId",
					"testCorrelationId",
					"test output",
					true)
				.getId()
				.toHexString();

		given()
			.contentType(ContentType.JSON)
			.when()
			.delete("/v1/reply/" + replyId)
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void deleteReplies() {
		String correlationId =
			testHelper.createCommandReplyEntity(
					"test-hardwareId",
					"testCorrelationId",
					"test output",
					true)
				.getCorrelationId();

		given()
			.contentType(ContentType.JSON)
			.when()
			.delete("/v1/reply/all/" + correlationId)
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void purgeWithDurationInDays() {
		commandService.saveRequest(
			testHelper.makeCommandRequestEntity(
					"test-hardwareId",
					"tag1",
					CommandType.e,
					ExecutionType.a)
				.setCommand("test"));

		testHelper.createCommandReplyEntity(
			"test-hardwareId",
			"testCorrelationId",
			"test output",
			true);

		given()
			.contentType(ContentType.JSON)
			.when()
			.delete("/v1/purge/0")
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void purge() {
		commandService.saveRequest(
			testHelper.makeCommandRequestEntity(
					"test-hardwareId",
					"tag1",
					CommandType.e,
					ExecutionType.a)
				.setCommand("test"));

		testHelper.createCommandReplyEntity(
			"test-hardwareId",
			"testCorrelationId",
			"test output",
			true);

		given()
			.contentType(ContentType.JSON)
			.when()
			.delete("/v1/purge")
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void replayCommand() {
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
							"test-hardwareId",
							"tag1",
							CommandType.e,
							ExecutionType.a)
						.setCommand("test"))
				.toHexString();

		given()
			.contentType(ContentType.JSON)
			.when()
			.put("/v1/replay/" + commandId)
			.then()
			.log().all()
			.statusCode(204);
	}
}
