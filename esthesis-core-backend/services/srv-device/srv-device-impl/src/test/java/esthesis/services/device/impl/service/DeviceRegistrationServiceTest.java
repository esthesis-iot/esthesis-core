package esthesis.services.device.impl.service;

import static esthesis.common.util.EsthesisCommonConstants.Device.Type.CORE;
import static esthesis.common.util.EsthesisCommonConstants.Device.Type.EDGE;
import static esthesis.core.common.AppConstants.Device.Status.PREREGISTERED;
import static esthesis.core.common.AppConstants.Device.Status.REGISTERED;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_PUSHED_TAGS;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_REGISTRATION_MODE;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_REGISTRATION_SECRET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.exception.QDisabledException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QSecurityException;
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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

	@Inject
	DeviceService deviceService;

	@SneakyThrows
	@BeforeEach
	void setUp() {
		testHelper.setup();

		// Mock the generation of a key pair request.
		when(keyResource.generateKeyPair()).thenReturn(
			new KeyPair(mock(PublicKey.class), mock(PrivateKey.class)));

		// Mock finding a tag.
		when(tagResource.findByName(anyString())).thenReturn(testHelper.makeTag("tag1"));

		// Mock the settings for enabling the device to push tags.
		when(settingsResource.findByName(DEVICE_PUSHED_TAGS))
			.thenReturn(new SettingEntity(DEVICE_PUSHED_TAGS.toString(), "true"));

	}

	@Test
	@SneakyThrows
	void registerOpenMode() {
		// Mock devices registration mode as "OPEN".
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN.name()));

		// Register a new CORE device.
		String coreDeviceId =
			deviceRegistrationService.register(new DeviceRegistrationDTO()
				.setHardwareId("new-core-device")
				.setTags(List.of("tag1"))
				.setType(CORE)).getId().toHexString();

		// Register a new EDGE device.
		String edgeDeviceId =
			deviceRegistrationService.register(new DeviceRegistrationDTO()
				.setHardwareId("new-edge-device")
				.setTags(List.of("tag2", "tag3"))
				.setType(EDGE)).getId().toHexString();

		// Assert CORE device was persisted.
		DeviceEntity coreDevice = deviceService.findById(coreDeviceId);
		assertNotNull(coreDevice);
		assertEquals("new-core-device", coreDevice.getHardwareId());
		assertEquals(CORE, coreDevice.getType());
		assertEquals(1, coreDevice.getTags().size());

		// Assert EDGE device was persisted.
		DeviceEntity edgeDevice = deviceService.findById(edgeDeviceId);
		assertNotNull(edgeDevice);
		assertEquals("new-edge-device", edgeDevice.getHardwareId());
		assertEquals(EDGE, edgeDevice.getType());
		assertEquals(2, edgeDevice.getTags().size());

		// Assert it doesn't allow registering a device with an already existing hardware-id.
		DeviceRegistrationDTO repeatedDevice =
			new DeviceRegistrationDTO().setHardwareId("new-core-device").setType(CORE);
		assertThrows(QAlreadyExistsException.class,
			() -> deviceRegistrationService.register(repeatedDevice));

		// Assert registration fails for invalid hardware name.
		DeviceRegistrationDTO invalidHardwareId1 =
			new DeviceRegistrationDTO().setHardwareId("invalid hardware id").setType(CORE);
		assertThrows(QMismatchException.class,
			() -> deviceRegistrationService.register(invalidHardwareId1));

		// Assert registration fails for invalid hardware name.
		DeviceRegistrationDTO invalidHardwareId2 =
			new DeviceRegistrationDTO().setHardwareId("#device@").setType(CORE);
		assertThrows(QMismatchException.class,
			() -> deviceRegistrationService.register(invalidHardwareId2));

		// Assert registration fails for invalid hardware name.
		DeviceRegistrationDTO invalidHardwareId3 =
			new DeviceRegistrationDTO().setHardwareId("@#$%&").setType(CORE);
		assertThrows(QMismatchException.class,
			() -> deviceRegistrationService.register(invalidHardwareId3));
	}


	@Test
	@SneakyThrows
	void registerOpenWithSecretMode() {
		// Mock devices registration mode as "OPEN WITH SECRET".
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN_WITH_SECRET.name()));

		// Mock the registration secret required for devices auto registration as "test-secret".
		when(settingsResource.findByName(DEVICE_REGISTRATION_SECRET))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_SECRET.toString(), "test-secret"));

		// Assert that a missing required secret doesn't allow registering the device.
		DeviceRegistrationDTO coreDeviceWithoutSecret =
			new DeviceRegistrationDTO().setHardwareId("new-core-device").setType(CORE);
		assertThrows(QSecurityException.class,
			() -> deviceRegistrationService.register(coreDeviceWithoutSecret));

		// Assert that a wrong registration secret doesn't allow registering the device.
		DeviceRegistrationDTO edgeDeviceWithWrongSecret =
			new DeviceRegistrationDTO().setHardwareId("new-edge-device").setType(EDGE)
				.setRegistrationSecret("wrong-value");
		assertThrows(QSecurityException.class,
			() -> deviceRegistrationService.register(edgeDeviceWithWrongSecret));

		// Perform the registering of a core and an edge device using the correct registration secret.
		String coreDeviceId =
			deviceRegistrationService.register(new DeviceRegistrationDTO()
				.setHardwareId("new-core-device")
				.setRegistrationSecret("test-secret")
				.setType(CORE)).getId().toHexString();

		String edgeDeviceId =
			deviceRegistrationService.register(new DeviceRegistrationDTO()
				.setHardwareId("new-edge-device")
				.setRegistrationSecret("test-secret")
				.setType(EDGE)).getId().toHexString();

		// Assert devices were persisted.
		assertNotNull(deviceService.findById(coreDeviceId));
		assertNotNull(deviceService.findById(edgeDeviceId));

	}


	@Test
	void registerDisabledMode() {
		// Mock devices registration mode as "DISABLED".
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.DISABLED.name()));

		// Assert core device registration is not enabled.
		DeviceRegistrationDTO coreDevice =
			new DeviceRegistrationDTO().setHardwareId("new-core-device").setType(CORE);
		assertThrows(QDisabledException.class, () -> deviceRegistrationService.register(coreDevice));

		// Assert edge device registration is not enabled.
		DeviceRegistrationDTO edgeDevice =
			new DeviceRegistrationDTO().setHardwareId("new-edge-device").setType(EDGE);
		assertThrows(QDisabledException.class, () -> deviceRegistrationService.register(edgeDevice));
	}


	@SneakyThrows
	@Test
	void registerIDMode() {
		// Mock devices registration mode as "OPEN".
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN.name()));

		// Pre-register a new core device.
		deviceRegistrationService.preregister(new DeviceRegistrationDTO()
			.setHardwareId("new-core-device")
			.setType(CORE));

		// Mock devices registration mode as "ID".
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.ID.name()));

		// Perform device registration in ID mode.
		String deviceId =
			deviceRegistrationService.register(
					new DeviceRegistrationDTO().setHardwareId("new-core-device"))
				.getId().toHexString();

		// Assert device has updated status from "PREREGISTERED" to "REGISTERED".
		assertEquals(REGISTERED, deviceService.findById(deviceId).getStatus());
	}

	@SneakyThrows
	@Test
	void preregister() {
		// Mock devices registration mode as "OPEN".
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN.name()));

		// Pre-register a new CORE device.
		deviceRegistrationService.preregister(new DeviceRegistrationDTO()
			.setHardwareId("new-core-device")
			.setType(CORE));

		// Pre-register a new EDGE device.
		deviceRegistrationService.preregister(new DeviceRegistrationDTO()
			.setHardwareId("new-edge-device")
			.setType(EDGE));

		// Assert devices were persisted with status "PREREGISTERED".
		assertEquals(PREREGISTERED,
			deviceService.findByHardwareIds("new-core-device").orElseThrow().getStatus());
		assertEquals(PREREGISTERED,
			deviceService.findByHardwareIds("new-edge-device").orElseThrow().getStatus());
	}

	@SneakyThrows
	@Test
	void activatePreregisteredDevice() {
		// Mock devices registration mode as "OPEN".
		when(settingsResource.findByName(DEVICE_REGISTRATION_MODE))
			.thenReturn(new SettingEntity(DEVICE_REGISTRATION_MODE.toString(),
				AppConstants.DeviceRegistrationMode.OPEN.name()));

		// Pre-register a new CORE device.
		deviceRegistrationService.preregister(new DeviceRegistrationDTO()
			.setHardwareId("new-core-device")
			.setType(CORE));

		// Assert device was activated.
		assertEquals(REGISTERED,
			deviceRegistrationService.activatePreregisteredDevice("new-core-device").getStatus());
	}
}
