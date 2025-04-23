package esthesis.service.dt.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.service.dt.impl.TestHelper;
import esthesis.service.dt.impl.security.DTSecurityFilterProvider;
import esthesis.service.dt.resource.DTResource;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagResource;
import esthesis.util.redis.RedisUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP;
import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_VALUE_TYPE;
import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(DTResource.class)
class DTResourceImplTest {

	@Inject
	TestHelper testHelper;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	TagResource tagResource;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	CommandSystemResource commandSystemResource;

	@InjectMock
	RedisUtils redisUtils;

	@InjectMock
	DTSecurityFilterProvider securityFilterProvider;

	@BeforeEach
	void setUp() {

		// Mock redis calls to retrieve the measurements.
		when(redisUtils.getFromHash(
			RedisUtils.KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-1"))
			.thenReturn("test-value-1");

		when(redisUtils.getFromHash(
			RedisUtils.KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-2"))
			.thenReturn("test-value-2");

		when(redisUtils.getFromHash(
			RedisUtils.KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-1." + REDIS_KEY_SUFFIX_VALUE_TYPE))
			.thenReturn("test-value-type");

		when(redisUtils.getFromHash(
			RedisUtils.KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-2." + REDIS_KEY_SUFFIX_VALUE_TYPE))
			.thenReturn("test-value-type");

		when(redisUtils.getFromHash(
			RedisUtils.KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-1." + REDIS_KEY_SUFFIX_TIMESTAMP))
			.thenReturn(Instant.now().toString());

		when(redisUtils.getFromHash(
			RedisUtils.KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-2." + REDIS_KEY_SUFFIX_TIMESTAMP))
			.thenReturn(Instant.now().toString());

		when(redisUtils.getHash(
			RedisUtils.KeyType.ESTHESIS_DM,
			"test-hardware-id"))
			.thenReturn(Map.of(
					"test-category.test-measurement-1", "test-value",
					"test-category.test-measurement-2", "test-value"
				)
			);

		// Mock command system resource count, getReplies and save.
		when(commandSystemResource.countCollectedReplies("test-correlation-id")).thenReturn(1L);
		when(commandSystemResource.getReplies("test-correlation-id")).thenReturn(testHelper.makeReplies(5));
		when(commandSystemResource.save(any(CommandRequestEntity.class))).thenReturn(testHelper.makeExecuteRequestScheduleInfo());

		// Mock tag resource findByName.
		TagEntity tagMock = new TagEntity("tag-test", "test-tag");
		tagMock.setId(new ObjectId());
		when(tagResource.findByName(anyString())).thenReturn(tagMock);


		// Mock security filter provider.
		doNothing().when(securityFilterProvider).filter(any());

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findJSON() {
		given()
			.accept(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.pathParam("category", "test-category")
			.pathParam("measurement", "test-measurement")
			.when().get("/v1/get/{hardwareId}/{category}/{measurement}")
			.then()
			.statusCode(204);
	}

	@Test
	void findPlain() {
		given()
			.accept(ContentType.TEXT)
			.pathParam("hardwareId", "test-hardware-id")
			.pathParam("category", "test-category")
			.pathParam("measurement", "test-measurement")
			.when().get("/v1/get/{hardwareId}/{category}/{measurement}")
			.then()
			.statusCode(204);
	}

	@Test
	void findAllJSON() {
		given()
			.accept(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.pathParam("category", "test-category")
			.when().get("/v1/get/{hardwareId}/{category}")
			.then()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	void findAllPlain() {
		given()
			.accept(ContentType.TEXT)
			.pathParam("hardwareId", "test-hardware-id")
			.pathParam("category", "test-category")
			.when().get("/v1/get/{hardwareId}/{category}")
			.then()
			.statusCode(200)
			.contentType(ContentType.TEXT);
	}

	@Test
	void findMeasurements() {
		given()
			.accept(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.pathParam("category", "test-category")
			.when().get("/v1/measurements/{hardwareId}/{category}")
			.then()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	void executeCommandByHardwareId() {
		given()
			.accept(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.queryParam("async", true)
			.body("test-command")
			.when().post("/v1/command/device/{hardwareId}/execute")
			.then()
			.statusCode(200);
	}

	@Test
	void executeCommandByTag() {
		given()
			.accept(ContentType.JSON)
			.pathParam("tag", "test-tag")
			.queryParam("async", true)
			.body("test-command")
			.when().post("/v1/command/tag/{tag}/execute")
			.then()
			.statusCode(200);
	}

	@Test
	void pingCommandByHardwareId() {
		given()
			.accept(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.queryParam("async", true)
			.when().post("/v1/command/device/{hardwareId}/ping")
			.then()
			.statusCode(200);
	}

	@Test
	void pingCommandByTag() {
		given()
			.accept(ContentType.JSON)
			.pathParam("tag", "test-tag")
			.queryParam("async", true)
			.when().post("/v1/command/tag/{tag}/ping")
			.then()
			.statusCode(200);
	}

	@Test
	void shutdownCommandByHardwareId() {
		given()
			.accept(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.queryParam("async", true)
			.when().post("/v1/command/device/{hardwareId}/shutdown")
			.then()
			.statusCode(200);
	}

	@Test
	void shutdownCommandByTag() {
		given()
			.accept(ContentType.JSON)
			.pathParam("tag", "test-tag")
			.queryParam("async", true)
			.when().post("/v1/command/tag/{tag}/shutdown")
			.then()
			.statusCode(200);
	}

	@Test
	void rebootCommandByHardwareId() {
		given()
			.accept(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.queryParam("async", true)
			.when().post("/v1/command/device/{hardwareId}/reboot")
			.then()
			.statusCode(200);
	}

	@Test
	void rebootCommandByTag() {
		given()
			.accept(ContentType.JSON)
			.pathParam("tag", "test-tag")
			.queryParam("async", true)
			.when().post("/v1/command/tag/{tag}/reboot")
			.then()
			.statusCode(200);
	}

	@Test
	void firmwareCommandByHardwareId() {
		given()
			.accept(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.queryParam("async", true)
			.when().post("/v1/command/device/{hardwareId}/firmware")
			.then()
			.statusCode(200);
	}

	@Test
	void firmwareCommandByTag() {
		given()
			.accept(ContentType.JSON)
			.pathParam("tag", "test-tag")
			.queryParam("async", true)
			.when().post("/v1/command/tag/{tag}/firmware")
			.then()
			.statusCode(200);
	}

	@Test
	void healthCommandByHardwareId() {
		given()
			.accept(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.queryParam("async", true)
			.when().post("/v1/command/device/{hardwareId}/health")
			.then()
			.statusCode(200);
	}

	@Test
	void healthCommandByTag() {
		given()
			.accept(ContentType.JSON)
			.pathParam("tag", "test-tag")
			.queryParam("async", true)
			.when().post("/v1/command/tag/{tag}/health")
			.then()
			.statusCode(200);
	}

	@Test
	void getCommandReply() {
		given()
			.accept(ContentType.JSON)
			.pathParam("correlationId", "test-correlation-id")
			.when().get("/v1/get/command/{correlationId}/reply")
			.then()
			.statusCode(200);
	}
}
