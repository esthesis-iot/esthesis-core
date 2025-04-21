package esthesis.services.device.impl.resource;

import esthesis.common.data.DataUtils;
import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Device.DataImportType;
import esthesis.core.common.AppConstants.KeyType;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.device.dto.DeviceProfileDTO;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.dto.DeviceTextDataImportDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.device.impl.service.DeviceService;
import esthesis.services.device.impl.service.TestHelper;
import esthesis.util.redis.RedisUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.List;

import static esthesis.common.util.EsthesisCommonConstants.Device.Type.CORE;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_PUSHED_TAGS;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_REGISTRATION_MODE;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_REGISTRATION_SECRET;
import static esthesis.core.common.AppConstants.NamedSetting.KAFKA_TOPIC_METADATA;
import static esthesis.core.common.AppConstants.NamedSetting.KAFKA_TOPIC_TELEMETRY;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(DeviceResource.class)
class DeviceResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Inject
	DeviceService deviceService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsResource settingsResource;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	TagResource tagResource;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	KeyResource keyResource;

	@InjectMock
	RedisUtils redisUtils;

	@BeforeEach
	void setUp() {
		testHelper.setup();

		// Mock setting allowing devices to push tags during registration.
		when(settingsResource.findByName(DEVICE_PUSHED_TAGS))
			.thenReturn(new SettingEntity(DEVICE_PUSHED_TAGS.toString(), "true"));

		// Mock registration secret.
		when(settingsResource.findByName(DEVICE_REGISTRATION_SECRET))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_SECRET.toString(), "test-secret"));

		// Mock device page fields.
		when(settingsResource.getDevicePageFields()).thenReturn(testHelper.getDevicePageFields());

		// Mock kafka topics for telemetry and metadata.
		when(settingsResource.findByName(AppConstants.NamedSetting.KAFKA_TOPIC_TELEMETRY))
			.thenReturn(new SettingEntity(KAFKA_TOPIC_TELEMETRY.toString(), "test-topic"));
		when(settingsResource.findByName(AppConstants.NamedSetting.KAFKA_TOPIC_METADATA))
			.thenReturn(new SettingEntity(KAFKA_TOPIC_METADATA.toString(), "test-topic"));

		// Mock geolocation lat and lon.
		when(settingsResource.findByName(AppConstants.NamedSetting.DEVICE_GEO_LAT))
			.thenReturn(new SettingEntity(AppConstants.NamedSetting.DEVICE_GEO_LAT.toString(), "0"));
		when(settingsResource.findByName(AppConstants.NamedSetting.DEVICE_GEO_LON))
			.thenReturn(new SettingEntity(AppConstants.NamedSetting.DEVICE_GEO_LON.toString(), "0"));

		// Mock redis relevant keys and values.
		when(redisUtils.getFromHash(eq(RedisUtils.KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn(
			"test");
		when(redisUtils.getLastUpdate(eq(RedisUtils.KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn(
			Instant.now());

		// Mock the tag resource requests.
		when(tagResource.findByNames("tag1")).thenReturn(List.of(testHelper.makeTag("tag1")));
		when(tagResource.findByNames("tag"))
			.thenReturn(List.of(testHelper.makeTag("tag1"), testHelper.makeTag("tag2")));
		when(tagResource.findByName(anyString())).thenReturn(testHelper.makeTag("tag1"));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void find() {
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				"tag1",
				CORE));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/find?page=0&size=10&sort=createdOn,desc")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", greaterThan(0));

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void get() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.pathParam("deviceId", device.getId().toHexString())
			.when().get("/v1/{deviceId}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void delete() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);

		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.pathParam("deviceId", device.getId().toHexString())
			.when().delete("/v1/{deviceId}")
			.then()
			.log().all()
			.statusCode(204);

		assertTrue(deviceService.findByHardwareIds("test-hardware-id").isEmpty());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void countByHardwareIds() {
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				"tag1",
				CORE));

		given()
			.accept(ContentType.JSON)
			.queryParam("hardwareIds", "test-hardware-id")
			.when().get("/v1/count/by-hardware-id")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByHardwareIds() {
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				"tag1",
				CORE));

		given()
			.accept(ContentType.JSON)
			.queryParam("hardwareIds", "test-hardware-id")
			.when().get("/v1/find/by-hardware-id")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", greaterThan(0));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByTagName() {
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				"tag1",
				CORE));

		given()
			.accept(ContentType.JSON)
			.queryParam("tag", "tag1")
			.when().get("/v1/find/by-tag-name")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByTagId() {
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				"tag1",
				CORE));

		given()
			.accept(ContentType.JSON)
			.queryParam("tag", testHelper.getTagId("tag1"))
			.when().get("/v1/find/by-tag-id")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void countByTags() {
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				"tag1",
				CORE));

		given()
			.accept(ContentType.JSON)
			.queryParam("tag", "tag1")
			.when().get("/v1/count/by-tag")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getDeviceGeolocation() {
		// Mock redis geolocation value returned.
		when(redisUtils.getFromHash(eq(RedisUtils.KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn("0");
		when(redisUtils.getLastUpdate(
			eq(RedisUtils.KeyType.ESTHESIS_DM),
			anyString(),
			anyString()))
			.thenReturn(Instant.now());

		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);

		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.pathParam("deviceId", device.getId().toHexString())
			.when().get("/v1/{deviceId}/geolocation")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void download() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);

		deviceService.getRepository().persist(device);

		// Public key download.
		given()
			.pathParam("deviceId", device.getId().toHexString())
			.queryParam("type", KeyType.PUBLIC)
			.when().get("/v1/{deviceId}/download")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.BINARY)
			.body(notNullValue());

		// Private key download.
		given()
			.pathParam("deviceId", device.getId().toHexString())
			.queryParam("type", KeyType.PRIVATE)
			.when().get("/v1/{deviceId}/download")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.BINARY)
			.body(notNullValue());

		// Certificate download.
		given()
			.pathParam("deviceId", device.getId().toHexString())
			.queryParam("type", KeyType.CERTIFICATE)
			.when().get("/v1/{deviceId}/download")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.BINARY)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getProfile() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);

		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.pathParam("deviceId", device.getId().toHexString())
			.when().get("/v1/{deviceId}/profile")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void saveProfile() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);

		deviceService.getRepository().persist(device);

		List<DeviceAttributeEntity> attributes = List.of(new DeviceAttributeEntity(
			device.getId().toHexString(),
			"test-attribute",
			"test-value",
			DataUtils.ValueType.STRING));

		DeviceProfileDTO deviceProfile = new DeviceProfileDTO().setAttributes(attributes).setFields(List.of());

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.pathParam("deviceId", device.getId().toHexString())
			.body(deviceProfile)
			.when().post("/v1/{deviceId}/profile")
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getDeviceData() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);

		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.pathParam("deviceId", device.getId().toHexString())
			.when().get("/v1/{deviceId}/device-data")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@SneakyThrows
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void preregister() {
		// Mock devices registration mode as "OPEN".
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN.name()));

		// Mock the generation of a key pair request.
		when(keyResource.generateKeyPair()).thenReturn(
			new KeyPair(mock(PublicKey.class), mock(PrivateKey.class)));

		DeviceRegistrationDTO deviceRegistrationDTO = new DeviceRegistrationDTO();
		deviceRegistrationDTO.setType(CORE);
		deviceRegistrationDTO.setHardwareId("test-hardware-id");
		deviceRegistrationDTO.setTags(List.of("tag1", "tag2"));

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(deviceRegistrationDTO)
			.when().post("/v1/preregister")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", greaterThan(0));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void activatePreregisteredDevice() {
		deviceService.getRepository().persist(testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.PREREGISTERED,
			"tag1",
			CORE));

		given()
			.accept(ContentType.JSON)
			.pathParam("hardwareId", "test-hardware-id")
			.when().put("/v1/activate/{hardwareId}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	@Disabled("Disabled while unexpected error is being investigated.")
	void saveTagsAndStatus() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);


		device.setStatus(AppConstants.Device.Status.DISABLED);
		device.setTags(List.of("tag1", "tag2"));

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(device)
			.when().post("/v1/tags-and-status") // Todo fix - findById(org.bson.types.ObjectId)" is null' while processing request to '/api/v1/tags-and-status'.
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void importDeviceDataFromText() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		DeviceTextDataImportDTO deviceTextDataImportDTO = new DeviceTextDataImportDTO();
		deviceTextDataImportDTO.setData("test-category test-measurement=10.1f 2025-01-01T00:00:00.000Z");
		deviceTextDataImportDTO.setBatchDelay(0);
		deviceTextDataImportDTO.setBatchSize(100);

		// Telemetry import.
		given()
			.contentType(ContentType.JSON)
			.pathParam("deviceId", device.getId().toHexString())
			.pathParam("type", DataImportType.TELEMETRY)
			.body(deviceTextDataImportDTO)
			.when().post("/v1/{deviceId}/import-data/{type}/text")
			.then()
			.log().all()
			.statusCode(204);


		// Metadata import.
		deviceTextDataImportDTO.setData("test-category test-metadata=test 2025-01-01T00:00:00.000Z");
		given()
			.contentType(ContentType.JSON)
			.pathParam("deviceId", device.getId().toHexString())
			.pathParam("type", DataImportType.METADATA)
			.body(deviceTextDataImportDTO)
			.when().post("/v1/{deviceId}/import-data/{type}/text")
			.then()
			.log().all()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void importDeviceDataFromFile() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		DeviceTextDataImportDTO deviceTextDataImportDTO = new DeviceTextDataImportDTO();
		deviceTextDataImportDTO.setData("test-category test-measurement=10.1f 2025-01-01T00:00:00.000Z");
		deviceTextDataImportDTO.setBatchDelay(0);
		deviceTextDataImportDTO.setBatchSize(100);

		// Telemetry import.
		given()
			.contentType(ContentType.MULTIPART)
			.pathParam("deviceId", device.getId().toHexString())
			.pathParam("type", DataImportType.TELEMETRY)
			.multiPart("dto", deviceTextDataImportDTO, MediaType.APPLICATION_JSON)
			.multiPart("file", "file.elp", deviceTextDataImportDTO.getData().getBytes(), MediaType.TEXT_PLAIN)
			.when().post("/v1/{deviceId}/import-data/{type}/file")
			.then()
			.log().all()
			.statusCode(204);


		// Metadata import.
		deviceTextDataImportDTO.setData("test-category test-metadata=test 2025-01-01T00:00:00.000Z");
		given()
			.contentType(ContentType.MULTIPART)
			.pathParam("deviceId", device.getId().toHexString())
			.pathParam("type", DataImportType.METADATA)
			.multiPart("dto", deviceTextDataImportDTO, MediaType.APPLICATION_JSON)
			.multiPart("file", "file.elp", deviceTextDataImportDTO.getData().getBytes(), MediaType.TEXT_PLAIN)
			.when().post("/v1/{deviceId}/import-data/{type}/file")
			.then()
			.log().all()
			.statusCode(204);
	}
}
