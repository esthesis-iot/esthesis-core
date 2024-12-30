package esthesis.services.device.impl.service;

import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.exception.QDisabledException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QSecurityException;
import esthesis.common.util.EsthesisCommonConstants;
import esthesis.core.common.AppConstants;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.resource.TagResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import static esthesis.common.util.EsthesisCommonConstants.Device.Type.CORE;
import static esthesis.core.common.AppConstants.Device.Status.REGISTERED;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_PUSHED_TAGS;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_REGISTRATION_MODE;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_REGISTRATION_SECRET;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class DeviceRegistrationServiceTest {

	@Inject
	TestHelper testHelper;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	KeyResource keyResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	TagResource tagResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsResource settingsResource;

	@Inject
	DeviceRegistrationService deviceRegistrationService;

	int initialDeviceSizeInDB = 0;
	int initialRegisteredDeviceSizeInDB = 0;
	int initialPreregisteredDeviceSizeInDB = 0;
	int initialDisabledDeviceSizeInDB = 0;
	int initialCoreDeviceSizeInDB = 0;
	int initialEdgeDeviceSizeInDB = 0;


	@SneakyThrows
	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		testHelper.createEntities();

		// Arrange mock generation of a key pair request
		when(keyResource.generateKeyPair()).thenReturn(new KeyPair(mock(PublicKey.class), mock(PrivateKey.class)));

		// Arrange mock tag resource requests
		when(tagResource.findByName(anyString(), anyBoolean())).thenReturn(testHelper.makeTag());

		// Arrange mock settings resource requests
		when(settingsResource.findByName(DEVICE_PUSHED_TAGS))
			.thenReturn(new SettingEntity(DEVICE_PUSHED_TAGS.toString(), "true"));
		when(settingsResource.findByName(DEVICE_REGISTRATION_SECRET))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_SECRET.toString(), "test-secret"));


		initialDeviceSizeInDB = testHelper.findAllDeviceEntity().size();
		initialRegisteredDeviceSizeInDB = testHelper.findAllRegisteredDeviceEntity().size();
		initialPreregisteredDeviceSizeInDB = testHelper.findAllPreregisteredDeviceEntity().size();
		initialDisabledDeviceSizeInDB = testHelper.findAllDisabledDeviceEntity().size();
		initialCoreDeviceSizeInDB = testHelper.findAllCoreDeviceEntity().size();
		initialEdgeDeviceSizeInDB = testHelper.findAllEdgeDeviceEntity().size();

		log.info("Initial device size in DB: {}", initialDeviceSizeInDB);
		log.info("Initial registered device size in DB: {}", initialRegisteredDeviceSizeInDB);
		log.info("Initial preregistered device size in DB: {}", initialPreregisteredDeviceSizeInDB);
		log.info("Initial disabled device size in DB: {}", initialDisabledDeviceSizeInDB);
		log.info("Initial core device size in DB: {}", initialCoreDeviceSizeInDB);
		log.info("Initial edge device size in DB: {}", initialEdgeDeviceSizeInDB);
	}

	@SneakyThrows
	@Test
	void registerOpenModeOK() {
		//Arrange new core device
		DeviceRegistrationDTO newCoreDevice = new DeviceRegistrationDTO();
		newCoreDevice.setHardwareId("new-core-device");
		newCoreDevice.setTags(List.of("tag1", "new-tag"));
		newCoreDevice.setType(CORE);

		// Arrange new edge device
		DeviceRegistrationDTO newEdgeDevice = new DeviceRegistrationDTO();
		newEdgeDevice.setHardwareId("new-edge-device");
		newEdgeDevice.setTags(List.of("tag3", "new-tag"));
		newEdgeDevice.setType(EsthesisCommonConstants.Device.Type.EDGE);

		// Arrange mock registration in OPEN mode
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN.name()));

		//Act
		deviceRegistrationService.register(newCoreDevice);
		deviceRegistrationService.register(newEdgeDevice);

		//Assert
		assertEquals(initialDeviceSizeInDB + 2, testHelper.findAllDeviceEntity().size());
		assertEquals(initialRegisteredDeviceSizeInDB + 2, testHelper.findAllRegisteredDeviceEntity().size());
		assertEquals(initialCoreDeviceSizeInDB + 1, testHelper.findAllCoreDeviceEntity().size());
		assertEquals(initialEdgeDeviceSizeInDB + 1, testHelper.findAllEdgeDeviceEntity().size());
		assertEquals(initialPreregisteredDeviceSizeInDB, testHelper.findAllPreregisteredDeviceEntity().size());
		assertEquals(initialDisabledDeviceSizeInDB, testHelper.findAllDisabledDeviceEntity().size());

	}


	@SneakyThrows
	@Test
	void registerOpenModeNOK() {
		//Arrange new core device with existing hardware id
		DeviceRegistrationDTO newCoreDevice = new DeviceRegistrationDTO();
		newCoreDevice.setHardwareId("test-registered-device-core-1");
		newCoreDevice.setTags(List.of("tag1", "new-tag"));
		newCoreDevice.setType(CORE);

		// Arrange new edge device with invalid hardware id
		DeviceRegistrationDTO newEdgeDevice = new DeviceRegistrationDTO();
		newEdgeDevice.setHardwareId("invalid hardware id");
		newEdgeDevice.setTags(List.of("tag3", "new-tag"));
		newEdgeDevice.setType(EsthesisCommonConstants.Device.Type.EDGE);

		// Arrange mock registration in OPEN mode
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN.name()));

		//Act & Assert throws exceptions
		assertThrows(QAlreadyExistsException.class, () -> deviceRegistrationService.register(newCoreDevice));
		assertThrows(QMismatchException.class, () -> deviceRegistrationService.register(newEdgeDevice));

		//Assert no changes were made to the database
		assertEquals(initialDeviceSizeInDB, testHelper.findAllDeviceEntity().size());

	}

	@Test
	void registerOpenWithSecretModeOK() {
		//Arrange new core device with expected secret
		DeviceRegistrationDTO newCoreDevice = new DeviceRegistrationDTO();
		newCoreDevice.setHardwareId("new-core-device");
		newCoreDevice.setTags(List.of("tag1", "new-tag"));
		newCoreDevice.setType(CORE);
		newCoreDevice.setRegistrationSecret("test-secret");

		// Arrange new edge device with expected secret
		DeviceRegistrationDTO newEdgeDevice = new DeviceRegistrationDTO();
		newEdgeDevice.setHardwareId("new-edge-device");
		newEdgeDevice.setTags(List.of("tag3", "new-tag"));
		newEdgeDevice.setType(EsthesisCommonConstants.Device.Type.EDGE);
		newEdgeDevice.setRegistrationSecret("test-secret");

		// Arrange mock registration in OPEN WITH SECRET mode
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN_WITH_SECRET.name()));

		//Act & Assert
		assertDoesNotThrow(() -> deviceRegistrationService.register(newCoreDevice));
		assertDoesNotThrow(() -> deviceRegistrationService.register(newEdgeDevice));

		//Assert new devices are registered
		assertEquals(initialDeviceSizeInDB + 2, testHelper.findAllDeviceEntity().size());
		assertEquals(initialCoreDeviceSizeInDB + 1, testHelper.findAllCoreDeviceEntity().size());
		assertEquals(initialEdgeDeviceSizeInDB + 1, testHelper.findAllEdgeDeviceEntity().size());

	}


	@Test
	void registerOpenWithSecretModeNOK() {
		//Arrange new core device without secret
		DeviceRegistrationDTO newCoreDevice = new DeviceRegistrationDTO();
		newCoreDevice.setHardwareId("new-core-device");
		newCoreDevice.setTags(List.of("tag1", "new-tag"));
		newCoreDevice.setType(CORE);

		// Arrange new edge device with invalid registration secret
		DeviceRegistrationDTO newEdgeDevice = new DeviceRegistrationDTO();
		newEdgeDevice.setHardwareId("new-edge-device");
		newEdgeDevice.setTags(List.of("tag3", "new-tag"));
		newEdgeDevice.setType(EsthesisCommonConstants.Device.Type.EDGE);
		newEdgeDevice.setRegistrationSecret("invalid-secret");

		// Arrange mock registration in OPEN WITH SECRET mode
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN_WITH_SECRET.name()));

		//Act & Assert
		assertThrows(QSecurityException.class, () -> deviceRegistrationService.register(newCoreDevice));
		assertThrows(QSecurityException.class, () -> deviceRegistrationService.register(newEdgeDevice));

		//Assert no changes were made to the database
		assertEquals(initialDeviceSizeInDB, testHelper.findAllDeviceEntity().size());
		assertEquals(initialCoreDeviceSizeInDB, testHelper.findAllCoreDeviceEntity().size());
		assertEquals(initialEdgeDeviceSizeInDB, testHelper.findAllEdgeDeviceEntity().size());

	}

	@Test
	void registerDisabledMode() {
		//Arrange new core device
		DeviceRegistrationDTO newCoreDevice = new DeviceRegistrationDTO();
		newCoreDevice.setHardwareId("new-core-device");
		newCoreDevice.setTags(List.of("tag1", "new-tag"));
		newCoreDevice.setType(CORE);

		// Arrange new edge device
		DeviceRegistrationDTO newEdgeDevice = new DeviceRegistrationDTO();
		newEdgeDevice.setHardwareId("new-edge-device");
		newEdgeDevice.setTags(List.of("tag3", "new-tag"));
		newEdgeDevice.setType(EsthesisCommonConstants.Device.Type.EDGE);

		// Arrange mock registration in DISABLED mode
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.DISABLED.name()));

		//Act & Assert throws exception
		assertThrows(QDisabledException.class, () -> deviceRegistrationService.register(newCoreDevice));
		assertThrows(QDisabledException.class, () -> deviceRegistrationService.register(newEdgeDevice));

		//Assert no changes were made to the database
		assertEquals(initialDeviceSizeInDB, testHelper.findAllDeviceEntity().size());
		assertEquals(initialCoreDeviceSizeInDB, testHelper.findAllCoreDeviceEntity().size());
		assertEquals(initialEdgeDeviceSizeInDB, testHelper.findAllEdgeDeviceEntity().size());

	}


	@SneakyThrows
	@Test
	void registerIDMode() {
		//Arrange find preregistered device
		DeviceEntity device = testHelper.findAllPreregisteredDeviceEntity().getFirst();

		//Arrange device registration from preregistered device
		DeviceRegistrationDTO deviceRegistration = new DeviceRegistrationDTO();
		deviceRegistration.setHardwareId(device.getHardwareId());

		// Arrange mock registration in ID mode
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.ID.name()));

		// Act
		deviceRegistrationService.register(deviceRegistration);

		// Assert device was updated
		DeviceEntity updatedDevice = testHelper.findDeviceByID(device.getId());
		assertEquals(deviceRegistration.getHardwareId(), updatedDevice.getHardwareId());
		assertEquals(REGISTERED, updatedDevice.getStatus());

		// Assert no changes were made to the database
		assertEquals(initialDeviceSizeInDB, testHelper.findAllDeviceEntity().size());

	}

	@SneakyThrows
	@Test
	void preregisterOK() {
		//Arrange new core device
		DeviceRegistrationDTO newCoreDevice = new DeviceRegistrationDTO();
		newCoreDevice.setHardwareId("new-core-device");
		newCoreDevice.setTags(List.of("tag1"));
		newCoreDevice.setType(CORE);

		// Arrange new edge device
		DeviceRegistrationDTO newEdgeDevice = new DeviceRegistrationDTO();
		newEdgeDevice.setHardwareId("new-edge-device");
		newEdgeDevice.setTags(List.of("tag3"));
		newEdgeDevice.setType(EsthesisCommonConstants.Device.Type.EDGE);

		// Arrange mock registration in OPEN mode
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN.name()));

		//Act
		deviceRegistrationService.preregister(newCoreDevice);
		deviceRegistrationService.preregister(newEdgeDevice);

		//Assert new devices were created as preregistered
		assertEquals(initialDeviceSizeInDB + 2, testHelper.findAllDeviceEntity().size());
		assertEquals(initialPreregisteredDeviceSizeInDB + 2, testHelper.findAllPreregisteredDeviceEntity().size());
		assertEquals(initialCoreDeviceSizeInDB + 1, testHelper.findAllCoreDeviceEntity().size());
		assertEquals(initialEdgeDeviceSizeInDB + 1, testHelper.findAllEdgeDeviceEntity().size());
		assertEquals(initialDisabledDeviceSizeInDB, testHelper.findAllDisabledDeviceEntity().size());
		assertEquals(initialRegisteredDeviceSizeInDB, testHelper.findAllRegisteredDeviceEntity().size());
	}

	@Test
	void preregisterNOK() {
		//Arrange new core device with existing hardware id
		DeviceRegistrationDTO newCoreDevice = new DeviceRegistrationDTO();
		newCoreDevice.setHardwareId("test-registered-device-core-1");
		newCoreDevice.setTags(List.of("tag1", "new-tag"));
		newCoreDevice.setType(CORE);

		// Arrange new edge device with invalid hardware id
		DeviceRegistrationDTO newEdgeDevice = new DeviceRegistrationDTO();
		newEdgeDevice.setHardwareId("@invalid@hardware$id");
		newEdgeDevice.setTags(List.of("tag3", "new-tag"));
		newEdgeDevice.setType(EsthesisCommonConstants.Device.Type.EDGE);

		// Arrange mock registration in OPEN mode
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN.name()));

		//Act & Assert throws exceptions
		assertThrows(QAlreadyExistsException.class, () -> deviceRegistrationService.preregister(newCoreDevice));
		assertThrows(QMismatchException.class, () -> deviceRegistrationService.preregister(newEdgeDevice));

		//Assert no changes were made to the database
		assertEquals(initialDeviceSizeInDB, testHelper.findAllDeviceEntity().size());
	}


	@Test
	void activatePreregisteredDevice() {
		// Arrange find preregistered device
		DeviceEntity preregisteredDevice = testHelper.findAllPreregisteredDeviceEntity().getFirst();

		// Act
		deviceRegistrationService.activatePreregisteredDevice(preregisteredDevice.getHardwareId());

		// Assert one device was updated from preregistered to registered
		assertEquals(initialDeviceSizeInDB, testHelper.findAllDeviceEntity().size());
		assertEquals(initialPreregisteredDeviceSizeInDB - 1, testHelper.findAllPreregisteredDeviceEntity().size());
		assertEquals(initialRegisteredDeviceSizeInDB + 1, testHelper.findAllRegisteredDeviceEntity().size());
	}
}
