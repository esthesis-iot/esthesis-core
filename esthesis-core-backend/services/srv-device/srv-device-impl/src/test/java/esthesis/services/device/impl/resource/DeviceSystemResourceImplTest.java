package esthesis.services.device.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
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
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@QuarkusTest
@TestHTTPEndpoint(DeviceSystemResource.class)
class DeviceSystemResourceImplTest {

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

	@SneakyThrows
	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void register() {
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
			.when().post("/v1/system/register")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void findByHardwareId() {
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				"tag1",
				CORE));

		given()
			.accept(ContentType.JSON)
			.queryParam("hardwareId", "test-hardware-id")
			.when().get("/v1/system/find/by-hardware-id")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void findById() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.queryParam("esthesisId", device.getId().toHexString())
			.when().get("/v1/system/find/by-id")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void findPublicKey() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.queryParam("hardwareId", device.getHardwareId())
			.when().get("/v1/system/public-key")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.TEXT)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getDeviceAttributesByEsthesisId() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.pathParam("esthesisId", device.getId().toHexString())
			.when().get("/v1/system/{esthesisId}/attributes-by-esthesis-id")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getDeviceAttributesByEsthesisHardwareId() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.pathParam("esthesisHardwareId", device.getHardwareId())
			.when().get("/v1/system/{esthesisHardwareId}/attributes-by-esthesis-hardware-id")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getDeviceIds() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/system/device-ids")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getDeviceAttributeByEsthesisHardwareIdAndAttributeName() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.pathParam("esthesisHardwareId", device.getHardwareId())
			.pathParam("attributeName", "test-attribute")
			.when().get("/v1/system/{esthesisHardwareId}/attribute-by-esthesis-id/{attributeName}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getDeviceStats() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/system/device-stats")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getDeviceTotalsStats() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/system/device-totals")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getLatestDevices() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.queryParam("limit", 10)
			.when().get("/v1/system/device-latest")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void findByTagNames() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.queryParam("tags", "tag1")
			.when().get("/v1/find/by-tag-names")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void findByTagIds() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		given()
			.accept(ContentType.JSON)
			.queryParam("tags", testHelper.getTagId("tag1"))
			.when().get("/v1/find/by-tag-ids")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}
}
