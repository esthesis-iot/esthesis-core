package esthesis.services.device.impl.service;

import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.data.DataUtils;
import esthesis.core.common.AppConstants.Device.Status;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.device.dto.DeviceKeyDTO;
import esthesis.service.device.dto.DeviceProfileDTO;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.dto.GeolocationDTO;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static esthesis.common.util.EsthesisCommonConstants.Device.Type.CORE;
import static esthesis.common.util.EsthesisCommonConstants.Device.Type.EDGE;
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

	int initialDeviceSizeInDB = 0;
	int initialRegisteredDeviceSizeInDB = 0;
	int initialPreregisteredDeviceSizeInDB = 0;
	int initialDisabledDeviceSizeInDB = 0;
	int initialCoreDeviceSizeInDB = 0;
	int initialEdgeDeviceSizeInDB = 0;
	int initialDeviceAttributeSizeInDB = 0;

	@BeforeEach
	void setUp() {
		testHelper.setup();
		testHelper.createEntities();

		// Mock the relevant settings
		when(settingsResource.findByName(DEVICE_PUSHED_TAGS))
			.thenReturn(new SettingEntity(DEVICE_PUSHED_TAGS.toString(), "true"));

		when(settingsResource.findByName(DEVICE_REGISTRATION_SECRET))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_SECRET.toString(), "test-secret"));

		when(settingsResource.getDevicePageFields()).thenReturn(testHelper.getDevicePageFields());

		when(settingsResource.findByName(NamedSetting.KAFKA_TOPIC_TELEMETRY))
			.thenReturn(new SettingEntity(KAFKA_TOPIC_TELEMETRY.toString(), "test-topic"));

		when(settingsResource.findByName(NamedSetting.KAFKA_TOPIC_METADATA))
			.thenReturn(new SettingEntity(KAFKA_TOPIC_METADATA.toString(), "test-topic"));

		when(settingsResource.findByName(NamedSetting.DEVICE_GEO_LAT))
			.thenReturn(new SettingEntity(NamedSetting.DEVICE_GEO_LAT.toString(), "0"));

		when(settingsResource.findByName(NamedSetting.DEVICE_GEO_LON))
			.thenReturn(new SettingEntity(NamedSetting.DEVICE_GEO_LON.toString(), "0"));

		// Mock redis
		when(redisUtils.getFromHash(eq(KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn("test");
		when(redisUtils.getLastUpdate(eq(KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn(Instant.now());
		when(redisUtils.getHashTriplets(eq(KeyType.ESTHESIS_DM), anyString())).thenReturn(testHelper.mockRedisHashTriplets());


		initialDeviceSizeInDB = testHelper.findAllDeviceEntity().size();
		initialRegisteredDeviceSizeInDB = testHelper.findAllRegisteredDeviceEntity().size();
		initialPreregisteredDeviceSizeInDB = testHelper.findAllPreregisteredDeviceEntity().size();
		initialDisabledDeviceSizeInDB = testHelper.findAllDisabledDeviceEntity().size();
		initialCoreDeviceSizeInDB = testHelper.findAllCoreDeviceEntity().size();
		initialEdgeDeviceSizeInDB = testHelper.findAllEdgeDeviceEntity().size();
		initialDeviceAttributeSizeInDB = testHelper.findAllDeviceAttributes().size();

		log.info("Initial device size in DB: {}", initialDeviceSizeInDB);
		log.info("Initial registered device size in DB: {}", initialRegisteredDeviceSizeInDB);
		log.info("Initial preregistered device size in DB: {}", initialPreregisteredDeviceSizeInDB);
		log.info("Initial disabled device size in DB: {}", initialDisabledDeviceSizeInDB);
		log.info("Initial core device size in DB: {}", initialCoreDeviceSizeInDB);
		log.info("Initial edge device size in DB: {}", initialEdgeDeviceSizeInDB);
		log.info("Initial device attribute size in DB: {}", initialDeviceAttributeSizeInDB);
	}

	@Test
	void findByHardwareId() {
		// Arrange existing and non-existing hardware ids
		String existingHardwareId = "test-registered-device-core-1";
		String existingHardwareIdPartial = "test-registered-device-core";
		String nonexistentHardwareId = "nonexistent-hardware-id";
		String nonexistentHardwareIdPartial = "nonexistent-hardware";

		// Act & Assert exact match
		DeviceEntity exisingDevice = deviceService.findByHardwareId(existingHardwareId, false).orElse(null);
		assertEquals("test-registered-device-core-1", Objects.requireNonNull(exisingDevice).getHardwareId());
		DeviceEntity nonExistingDevice = deviceService.findByHardwareId(nonexistentHardwareId, false).orElse(null);
		assertNull(nonExistingDevice);

		// Act & Assert partial match
		DeviceEntity exisingDevicePartial = deviceService.findByHardwareId(existingHardwareIdPartial, true).orElse(null);
		assertNotNull(exisingDevicePartial);
		DeviceEntity nonExistingDevicePartial = deviceService.findByHardwareId(nonexistentHardwareIdPartial, true).orElse(null);
		assertNull(nonExistingDevicePartial);
	}

	@Test
	void findByHardwareIdList() {
		// Arrange existing and non-existing hardware ids
		String existingHardwareId = "test-registered-device-core-1";
		String existingHardwareIdPartial = "test-registered-device-core";
		String nonexistentHardwareId = "nonexistent-hardware-id";
		String nonexistentHardwareIdPartial = "nonexistent-hardware";

		// Act & Assert exact match should found 1  existing device and 0 non-existing devices
		assertEquals(1, deviceService.findByHardwareId(List.of(existingHardwareId), false).size());
		assertEquals(0, deviceService.findByHardwareId(List.of(nonexistentHardwareId), false).size());

		// Act & Assert partial match should found 2 existing devices and 0 non-existing devices
		assertEquals(2, deviceService.findByHardwareId(List.of(existingHardwareIdPartial), true).size());
		assertEquals(0, deviceService.findByHardwareId(List.of(nonexistentHardwareIdPartial), true).size());
	}


	@Test
	void countByHardwareId() {
		// Arrange existing and non-existing hardware ids
		String existingHardwareId = "test-registered-device-core-1";
		String existingHardwareIdPartial = "test-registered-device-core";
		String nonexistentHardwareId = "nonexistent-hardware-id";
		String nonexistentHardwareIdPartial = "nonexistent-hardware";

		// Act
		long existingDeviceCount = deviceService.countByHardwareId(List.of(existingHardwareId), false);
		long existingDeviceCountPartial = deviceService.countByHardwareId(List.of(existingHardwareIdPartial), true);
		long nonExistingDeviceCount = deviceService.countByHardwareId(List.of(nonexistentHardwareId), false);
		long nonExistingDeviceCountPartial = deviceService.countByHardwareId(List.of(nonexistentHardwareIdPartial), true);

		// Assert
		assertEquals(1, existingDeviceCount);
		assertEquals(2, existingDeviceCountPartial);
		assertEquals(0, nonExistingDeviceCount);
		assertEquals(0, nonExistingDeviceCountPartial);
	}

	@Test
	void getPublicKey() {
		// Arrange an existing device id
		String exisingDeviceId = testHelper.findAllDeviceEntity().getFirst().getId().toString();

		// Act & Assert
		String exisingDevicePublicKey = deviceService.getPublicKey(exisingDeviceId);
		assertEquals("test-public-key", exisingDevicePublicKey);
	}

	@Test
	void getPrivateKey() {
		// Arrange an existing device id
		String exisingDeviceId = testHelper.findAllDeviceEntity().getFirst().getId().toString();

		// Act & Assert
		String exisingDevicePrivateKey = deviceService.getPrivateKey(exisingDeviceId);
		assertEquals("test-private-key", exisingDevicePrivateKey);
	}

	@Test
	void getCertificate() {
		// Arrange an existing device id
		String exisingDeviceId = testHelper.findAllDeviceEntity().getFirst().getId().toString();

		// Act & Assert
		String exisingDeviceCertificate = deviceService.getCertificate(exisingDeviceId);
		assertEquals("test-certificate", exisingDeviceCertificate);
	}

	@Test
	void saveProfile() {
		// Arrange - Prepare a new device profile
		DeviceEntity existingDevice = testHelper.findAllDeviceEntity().getFirst();
		String existingAttributeName = "test-boolean-attribute-" + existingDevice.getHardwareId();
		String newAttributeName = "test-string-attribute-" + existingDevice.getHardwareId();

		DeviceProfileDTO profile = new DeviceProfileDTO();
		profile.setAttributes(List.of(
			new DeviceAttributeEntity(
				existingDevice.getId().toString(),
				existingAttributeName,
				"false",
				DataUtils.ValueType.BOOLEAN),
			new DeviceAttributeEntity(
				existingDevice.getId().toString(),
				newAttributeName,
				"test-value",
				DataUtils.ValueType.STRING)
		));

		// Act
		deviceService.saveProfile(existingDevice.getId().toString(), profile);

		// Assert - Verify the new attribute is added
		List<DeviceAttributeEntity> deviceAttributes = testHelper.findAllDeviceAttributesByDeviceId(existingDevice.getId().toString());
		assertEquals(2, deviceAttributes.size());

		int expectedSize = initialDeviceAttributeSizeInDB + 1;
		assertEquals(expectedSize, testHelper.findAllDeviceAttributes().size());

		// Assert - Verify the existing boolean attribute is updated
		DeviceAttributeEntity existingAttribute =
			deviceAttributes.stream()
				.filter(d -> d.getAttributeName().equals(existingAttributeName))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Existing attribute not found"));

		assertEquals("false", existingAttribute.getAttributeValue());
	}

	@Test
	void getProfile() {
		// Arrange - Find an existing device
		DeviceEntity existingDevice = testHelper.findAllDeviceEntity().getFirst();

		// Act
		DeviceProfileDTO profile = deviceService.getProfile(existingDevice.getId().toString());
		List<DeviceAttributeEntity> attributes = profile.getAttributes();

		// Assert - Verify the existing boolean attribute is returned
		String expectedAtributeName = "test-boolean-attribute-" + existingDevice.getHardwareId();
		assertEquals(1, attributes.size());
		assertEquals(expectedAtributeName, attributes.getFirst().getAttributeName());
	}

	@Test
	void getDeviceData() {
		// Arrange - Find an existing device
		DeviceEntity existingDevice = testHelper.findAllDeviceEntity().getFirst();
		String deviceId = existingDevice.getId().toString();

		// Act
		List<DeviceProfileFieldDataDTO> profileFieldDataDTOS = deviceService.getDeviceData(deviceId);

		// Assert
		assertEquals(3, profileFieldDataDTOS.size());
	}

	@Test
	void getDeviceAttributeByName() {
		// Arrange - Find an existing and non-existing device attribute name
		DeviceEntity existingDevice = testHelper.findAllDeviceEntity().getFirst();
		String existingAttributeName = "test-boolean-attribute-" + existingDevice.getHardwareId();
		String nonexistentAttributeName = "nonexistent-attribute-name";

		// Act
		Optional<DeviceAttributeEntity> existingAttribute =
			deviceService.getDeviceAttributeByName(existingDevice.getId().toString(), existingAttributeName);
		Optional<DeviceAttributeEntity> nonexistentAttribute =
			deviceService.getDeviceAttributeByName(existingDevice.getId().toString(), nonexistentAttributeName);

		// Assert
		assertTrue(existingAttribute.isPresent());
		assertFalse(nonexistentAttribute.isPresent());

	}

	@Test
	void deleteById() {
		// Arrange - Prepare an existing and non-existing device id
		DeviceEntity existingDevice = testHelper.findAllDeviceEntity().getFirst();
		String existingDeviceId = existingDevice.getId().toString();
		String nonExistingDeviceId = new ObjectId().toString();

		// Act & Assert - Verify if none  device was deleted by the non-existing id
		deviceService.deleteById(nonExistingDeviceId);
		assertEquals(initialDeviceSizeInDB, testHelper.findAllDeviceEntity().size());
		assertEquals(initialDeviceAttributeSizeInDB, testHelper.findAllDeviceAttributes().size());

		// Act & Assert - Verify if the existing device was deleted
		deviceService.deleteById(existingDeviceId);
		assertEquals(initialDeviceSizeInDB - 1, testHelper.findAllDeviceEntity().size());
		assertEquals(initialDeviceAttributeSizeInDB - 1, testHelper.findAllDeviceAttributes().size());

	}

	@Test
	void getDevicesIds() {
		// Act
		List<String> devicesIds = deviceService.getDevicesIds();

		// Assert - Verify the number of ids found matches the number of devices
		assertEquals(initialDeviceSizeInDB, devicesIds.size());
	}

	@SneakyThrows
	@Test
	void importData() {
		// Arrange
		String deviceId = testHelper.findAllDeviceEntity().getFirst().getId().toString();
		BufferedReader reader = testHelper.getBufferedReaderForImportData();
		MessageTypeEnum messageTypeTelemetry = MessageTypeEnum.T;
		MessageTypeEnum messageTypeMetadata = MessageTypeEnum.M;

		ImportDataProcessingInstructionsDTO instructions =
			ImportDataProcessingInstructionsDTO.builder()
				.batchSize(10)
				.batchDelay(100)
				.build();

		// Act & Assert
		assertDoesNotThrow(() -> deviceService.importData(deviceId, reader, messageTypeTelemetry, instructions));
		assertDoesNotThrow(() -> deviceService.importData(deviceId, reader, messageTypeMetadata, instructions));


	}

	@Test
	void find() {
		// Act - Find all devices
		List<DeviceEntity> devices = deviceService.find(testHelper.makePageable(0, 100), true).getContent();

		// Assert
		assertEquals(initialDeviceSizeInDB, devices.size());

	}

	@Test
	void findById() {
		// Arrange - Prepare an existing and non-existing device id
		String existingDeviceId = testHelper.findAllDeviceEntity().getFirst().getId().toString();
		String nonExistingDeviceId = new ObjectId().toString();

		// Act
		DeviceEntity existingDevice = deviceService.findById(existingDeviceId);
		DeviceEntity nonExistingDevice = deviceService.findById(nonExistingDeviceId);

		// Assert
		assertNotNull(existingDevice);
		assertNull(nonExistingDevice);
	}

	@Test
	void saveNew() {
		// Arrange - Prepare new core and edge devices
		DeviceEntity newCoreDevice = new DeviceEntity();
		newCoreDevice.setHardwareId("new-test-device-1");
		newCoreDevice.setTags(List.of("tag1", "tag2"));
		newCoreDevice.setDeviceKey(
			new DeviceKeyDTO()
				.setPrivateKey("test-private-key")
				.setPublicKey("test-public-key")
				.setCertificate("test-certificate")
		);
		newCoreDevice.setType(CORE);

		DeviceEntity newEdgeDevice = new DeviceEntity();
		newEdgeDevice.setHardwareId("new-test-device-2");
		newEdgeDevice.setTags(List.of("tag1", "tag2"));
		newEdgeDevice.setDeviceKey(
			new DeviceKeyDTO()
				.setPrivateKey("test-private-key")
				.setPublicKey("test-public-key")
				.setCertificate("test-certificate")
		);
		newEdgeDevice.setType(EDGE);

		// Act
		deviceService.saveNew(newCoreDevice);
		deviceService.saveNew(newEdgeDevice);

		// Assert - Verify new devices were created
		assertEquals(initialDeviceSizeInDB + 2, testHelper.findAllDeviceEntity().size());
		assertEquals(initialCoreDeviceSizeInDB + 1, testHelper.findAllCoreDeviceEntity().size());
		assertEquals(initialEdgeDeviceSizeInDB + 1, testHelper.findAllEdgeDeviceEntity().size());

	}

	@Test
	void saveUpdate() {
		// Arrange - Update an existing device
		DeviceEntity existingDevice = testHelper.findAllDeviceEntity().getFirst();

		existingDevice.setStatus(Status.DISABLED);
		existingDevice.setLastSeen(Instant.now());
		existingDevice.setTags(List.of("tag1", "tag2"));

		// Act
		deviceService.saveUpdate(existingDevice);

		// Assert - Verify the device was updated and not created
		assertEquals(initialDeviceSizeInDB, testHelper.findAllDeviceEntity().size());
	}

	@Test
	void getGeolocation() {
		// Arrange - Prepare an existing device id
		String existingDeviceId = testHelper.findAllDeviceEntity().getFirst().getId().toString();

		// Mock
		when(redisUtils.getFromHash(eq(KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn("0");
		when(redisUtils.getLastUpdate(eq(KeyType.ESTHESIS_DM), anyString(), anyString())).thenReturn(Instant.now());

		// Act
		GeolocationDTO geolocation = deviceService.getGeolocation(existingDeviceId);

		// Assert
		assertNotNull(geolocation);
	}
}
