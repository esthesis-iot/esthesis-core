package esthesis.services.device.impl.service;

import static esthesis.common.util.EsthesisCommonConstants.Device.Type.CORE;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_PUSHED_TAGS;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_REGISTRATION_SECRET;
import static esthesis.core.common.AppConstants.NamedSetting.KAFKA_TOPIC_METADATA;
import static esthesis.core.common.AppConstants.NamedSetting.KAFKA_TOPIC_TELEMETRY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.data.DataUtils;
import esthesis.core.common.AppConstants.Device.Status;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.device.dto.DeviceProfileDTO;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.dto.ImportDataProcessingInstructionsDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
@QuarkusTest
class DeviceServiceTest {

	@Inject
	TestHelper testHelper;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsResource settingsResource;

	@InjectMock
	RedisUtils redisUtils;

	@Inject
	DeviceService deviceService;

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
		when(settingsResource.findByName(NamedSetting.KAFKA_TOPIC_TELEMETRY))
			.thenReturn(new SettingEntity(KAFKA_TOPIC_TELEMETRY.toString(), "test-topic"));
		when(settingsResource.findByName(NamedSetting.KAFKA_TOPIC_METADATA))
			.thenReturn(new SettingEntity(KAFKA_TOPIC_METADATA.toString(), "test-topic"));

		// Mock geolocation lat and lon.
		when(settingsResource.findByName(NamedSetting.DEVICE_GEO_LAT))
			.thenReturn(new SettingEntity(NamedSetting.DEVICE_GEO_LAT.toString(), "0"));
		when(settingsResource.findByName(NamedSetting.DEVICE_GEO_LON))
			.thenReturn(new SettingEntity(NamedSetting.DEVICE_GEO_LON.toString(), "0"));

		// Mock redis relevant keys and values.
		when(redisUtils.getFromHash(eq(KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn(
			"test");
		when(redisUtils.getLastUpdate(eq(KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn(
			Instant.now());


	}

	@Test
	void findByHardwareId() {

		// Perform a save operation for a new device.
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				Status.REGISTERED,
				"tag1",
				CORE));

		// Assert the device can be found by its hardware id.
		assertTrue(deviceService.findByHardwareIds("test-hardware-id").isPresent());
		assertFalse(deviceService.findByHardwareIds("test-hardware").isPresent());

		// Assert non-existing hardware id cannot found any device.
		assertFalse(deviceService.findByHardwareIds("non-existing-hardware-id").isPresent());
		assertFalse(deviceService.findByHardwareIds("non-existing-hardware").isPresent());

	}

	@Test
	void findByHardwareIdList() {
		// Perform a save operation for a new device.
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				Status.REGISTERED,
				"tag1",
				CORE));

		// Assert the device can be found by its hardware id.
		assertFalse(deviceService.findByHardwareIds(List.of("test-hardware-id")).isEmpty());

		// Assert non-existing hardware id cannot found any device.
		assertTrue(
			deviceService.findByHardwareIds(List.of("non-existing-hardware-id")).isEmpty());
		assertTrue(deviceService.findByHardwareIds(List.of("non-existing-hardware-id")).isEmpty());
		assertTrue(deviceService.findByHardwareIds(List.of("non-existing-hardware")).isEmpty());
	}


	@Test
	void countByHardwareId() {
		// Assert count is 0 when no device exists.
		assertEquals(0, deviceService.countByHardwareId(List.of("test-hardware-id")));

		// Perform a save operation for a new device.
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				Status.REGISTERED,
				"tag1",
				CORE));

		//Assert count is 1 when one device exists.
		assertEquals(1, deviceService.countByHardwareId(List.of("test-hardware-id")));
		assertEquals(0, deviceService.countByHardwareId(List.of("test-hardware")));

		// Assert count is 0 when hardware id does not exist.
		assertEquals(0, deviceService.countByHardwareId(List.of("non-existing-hardware-id")));
	}

	@Test
	void getPublicKey() {
		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Assert the public key is found for the provided device id.
		assertEquals("test-public-key", deviceService.getPublicKey(device.getId().toHexString()));
	}

	@Test
	void getPrivateKey() {
		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Assert the private key is found for the provided device id.
		assertEquals("test-private-key", deviceService.getPrivateKey(device.getId().toHexString()));
	}

	@Test
	void getCertificate() {
		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Assert the certificate is found for the provided device id.
		assertEquals("test-certificate", deviceService.getCertificate(device.getId().toHexString()));
	}

	@Test
	void saveProfile() {
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		// Perform a save operation for a new device.
		deviceService.getRepository().persist(device);

		// Assert the attributes list is empty.
		assertTrue(deviceService.getProfile(device.getId().toHexString()).getAttributes().isEmpty());

		// Save a new profile for the device.
		deviceService.saveProfile(device.getId().toHexString(),
			new DeviceProfileDTO().setAttributes(List.of(
				new DeviceAttributeEntity(
					device.getId().toHexString(),
					"test-boolean-attribute",
					"false",
					DataUtils.ValueType.BOOLEAN))));

		// Assert the attributes list is not empty.
		assertFalse(deviceService.getProfile(device.getId().toHexString()).getAttributes().isEmpty());
	}

	@Test
	void getProfile() {
		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Save a new profile for the device.
		deviceService.saveProfile(device.getId().toHexString(),
			new DeviceProfileDTO()
				.setAttributes(
					List.of(
						new DeviceAttributeEntity(
							device.getId().toHexString(),
							"test-boolean-attribute",
							"false",
							DataUtils.ValueType.BOOLEAN))
				));

		// Assert profile is found and has the expected attribute.
		DeviceAttributeEntity attribute = deviceService.getProfile(device.getId().toHexString())
			.getAttributes().getFirst();
		assertEquals("test-boolean-attribute", attribute.getAttributeName());
		assertEquals("false", attribute.getAttributeValue());
		assertEquals(DataUtils.ValueType.BOOLEAN, attribute.getAttributeType());
	}

	@Test
	void getDeviceData() {
		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Save a new profile for the device.
		deviceService.saveProfile(device.getId().toHexString(),
			new DeviceProfileDTO()
				.setFields(List.of(new DeviceProfileFieldDataDTO(
					"test-label",
					"test-value",
					"test-value-type",
					Instant.now(),
					"test-icon")))
				.setAttributes(List.of())
		);

		// Mock the redis hash triplets where the device data is stored.
		when(redisUtils.getHashTriplets(eq(KeyType.ESTHESIS_DM), anyString())).thenReturn(
			testHelper.mockRedisHashTriplets("test-label", "test-value"));

		// Assert device data was persisted.
		DeviceProfileFieldDataDTO deviceProfileFieldDataDTO =
			deviceService.getDeviceData(device.getId().toHexString()).getFirst();
		assertEquals("test-label", deviceProfileFieldDataDTO.getLabel());
		assertEquals("test-value", deviceProfileFieldDataDTO.getValue());
	}

	@Test
	void getDeviceAttributeByName() {
		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Save a new profile for the device.
		deviceService.saveProfile(device.getId().toHexString(),
			new DeviceProfileDTO()
				.setAttributes(
					List.of(
						new DeviceAttributeEntity(
							device.getId().toHexString(),
							"test-boolean-attribute",
							"false",
							DataUtils.ValueType.BOOLEAN))
				));
		// Assert valid device attribute was found.
		assertTrue(
			deviceService.getDeviceAttributeByName(device.getId().toHexString(), "test-boolean-attribute")
				.isPresent());

		// Assert non-existing device or attribute was not found.
		assertFalse(
			deviceService.getDeviceAttributeByName(device.getId().toHexString(), "non-existing-attribute")
				.isPresent());
		assertFalse(deviceService.getDeviceAttributeByName(
			new ObjectId().toHexString(),
			"test-boolean-attribute").isPresent());


	}

	@Test
	void deleteById() {
		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Assert the device was saved.
		assertNotNull(deviceService.findById(device.getId().toHexString()));

		// Perform a delete operation for the device.
		deviceService.deleteById(device.getId().toHexString());

		// Assert the device was deleted.
		assertNull(deviceService.findById(device.getId().toHexString()));

	}

	@Test
	void getDevicesIds() {
		// Assert device ids list is empty.
		assertTrue(deviceService.getDevicesIds().isEmpty());

		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Assert device id is found.
		assertTrue(deviceService.getDevicesIds().contains(device.getId().toHexString()));
	}

	@SneakyThrows
	@Test
	void importData() {
		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Arrange and prepare import data instructions DTO.
		ImportDataProcessingInstructionsDTO instructions =
			ImportDataProcessingInstructionsDTO.builder()
				.batchSize(10)
				.batchDelay(100)
				.build();

		// Assert import data operation is successful.
		assertDoesNotThrow(
			() -> deviceService.importData(
				device.getId().toHexString(),
				testHelper.getBufferedReaderForImportData(),
				MessageTypeEnum.T,
				instructions)
		);

		assertDoesNotThrow(
			() -> deviceService.importData(
				device.getId().toHexString(),
				testHelper.getBufferedReaderForImportData(),
				MessageTypeEnum.M,
				instructions)
		);


	}

	@Test
	void find() {
		// Assert find no devices.
		assertTrue(deviceService.find(testHelper.makePageable(0, 100)).getContent().isEmpty());

		// Perform a save operation for a new device.
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				Status.REGISTERED,
				"tag1",
				CORE));

		// Assert device is found.
		assertEquals(1,
			deviceService.find(testHelper.makePageable(0, 100)).getContent().size());
	}

	@Test
	void findById() {
		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Assert device is found.
		assertNotNull(deviceService.findById(device.getId().toHexString()));

		// Assert non-existing device is not found.
		assertNull(deviceService.findById(new ObjectId().toHexString()));
	}

	@Test
	void getGeolocation() {
		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		// Mock redis geolocation value returned.
		when(redisUtils.getFromHash(eq(KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn("0");
		when(redisUtils.getLastUpdate(eq(KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn(
			Instant.now());

		// Assert device geolocation is found.
		assertNotNull(deviceService.getGeolocation(device.getId().toHexString()));
	}
}
