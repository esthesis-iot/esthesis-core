package esthesis.services.device.impl.service;

import static esthesis.common.util.EsthesisCommonConstants.Device.Type.CORE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import esthesis.core.common.AppConstants;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.tag.resource.TagResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
@QuarkusTest
class DeviceTagServiceTest {

	@Inject
	TestHelper testHelper;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	TagResource tagResource;

	@Inject
	DeviceTagService deviceTagService;

	@Inject
	DeviceService deviceService;

	@BeforeEach
	void setUp() {
		testHelper.setup();

		// Mock the tag resource requests.
		when(tagResource.findByNames(eq("tag1"))).thenReturn(List.of(testHelper.makeTag("tag1")));
		when(tagResource.findByNames(eq("tag")))
			.thenReturn(List.of(testHelper.makeTag("tag1"), testHelper.makeTag("tag2")));
	}

	@Test
	void findByTagName() {
		// Perform a save operation for a new device.
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				testHelper.getTagId("tag1"),
				CORE));

		// Assert device can be found by existing tag name.
		assertFalse(deviceTagService.findByTagName("tag1").isEmpty());

		// Assert non-existing tag can't find any device.
		assertTrue(deviceTagService.findByTagName("nonExistentTag").isEmpty());
	}

	@Test
	void findByTagNameList() {
		// Perform a save operation for a new device.
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				testHelper.getTagId("tag1"),
				CORE));

		// Assert device can be found by existing tag name.
		assertFalse(deviceTagService.findByTagName(List.of("tag1")).isEmpty());

		// Assert non-existing tag can't find any device.
		assertTrue(deviceTagService.findByTagName(List.of("nonExistentTag")).isEmpty());
	}

	@Test
	void findByTagId() {
		// Perform a save operation for a new device.
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				testHelper.getTagId("tag1"),
				CORE));

		// Assert device can be found by its tag id.
		assertNotNull(deviceTagService.findByTagId(testHelper.getTagId("tag1")));

		// Assert non-existing tag id can't find any device.
		assertTrue(deviceTagService.findByTagId("nonExistentTag").isEmpty());
	}

	@Test
	void countByTag() {
		// Perform a save operation for a new device.
		deviceService.getRepository().persist(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				testHelper.getTagId("tag1"),
				CORE));

		// Assert existing tags count one device.
		assertEquals(1, deviceTagService.countByTag(List.of("tag1")));

		// Assert non-existing tags can't count any devices.
		assertEquals(0, deviceTagService.countByTag(List.of("nonExistentTag")));
	}

	@Test
	void removeTagById() {

		// Perform a save operation for a new device.
		DeviceEntity device = testHelper.makeDeviceEntity(
			"test-hardware-id",
			AppConstants.Device.Status.REGISTERED,
			"tag1",
			CORE);
		deviceService.getRepository().persist(device);

		//Assert no tag is removed when tag does not exist.
		assertDoesNotThrow(() -> deviceTagService.removeTagById("nonExistentTag"));
		assertFalse(deviceService.findById(device.getId().toHexString()).getTags().isEmpty());

		// Assert tag is removed when tag exists.
		assertDoesNotThrow(() -> deviceTagService.removeTagById("tag1"));
		assertTrue(deviceService.findById(device.getId().toHexString()).getTags().isEmpty());

	}
}
