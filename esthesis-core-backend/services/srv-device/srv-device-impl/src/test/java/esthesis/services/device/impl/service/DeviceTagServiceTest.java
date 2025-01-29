package esthesis.services.device.impl.service;

import esthesis.core.common.AppConstants;
import esthesis.service.tag.resource.TagResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.common.util.EsthesisCommonConstants.Device.Type.CORE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
		when(tagResource.findByNames("tag1", anyBoolean())).thenReturn(List.of(testHelper.makeTag("tag1")));
		when(tagResource.findByNames("tag", eq(true)))
			.thenReturn(List.of(testHelper.makeTag("tag1"), testHelper.makeTag("tag2")));
	}

	@Test
	void findByTagName() {
		// Perform a save operation for a new device.
		deviceService.saveNew(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				testHelper.getTagId("tag1"),
				CORE)).getId().toHexString();

		// Assert device can be found by existing tag name.
		assertFalse(deviceTagService.findByTagName("tag1", false).isEmpty());
		assertFalse(deviceTagService.findByTagName("tag1", true).isEmpty());
		assertFalse(deviceTagService.findByTagName("tag", true).isEmpty());

		// Assert non-existing tag can't find any device.
		assertTrue(deviceTagService.findByTagName("nonExistentTag", false).isEmpty());
		assertTrue(deviceTagService.findByTagName("nonExistentTag", true).isEmpty());
	}

	@Test
	void findByTagNameList() {
		// Perform a save operation for a new device.
		deviceService.saveNew(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				testHelper.getTagId("tag1"),
				CORE)).getId().toHexString();

		// Assert device can be found by existing tag name.
		assertFalse(deviceTagService.findByTagName(List.of("tag1"), false).isEmpty());
		assertFalse(deviceTagService.findByTagName(List.of("tag1"), true).isEmpty());
		assertFalse(deviceTagService.findByTagName(List.of("tag"), true).isEmpty());

		// Assert non-existing tag can't find any device.
		assertTrue(deviceTagService.findByTagName(List.of("nonExistentTag"), false).isEmpty());
		assertTrue(deviceTagService.findByTagName(List.of("nonExistentTag"), true).isEmpty());
	}

	@Test
	void findByTagId() {
		// Perform a save operation for a new device.
		deviceService.saveNew(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				testHelper.getTagId("tag1"),
				CORE)).getId().toHexString();

		// Assert device can be found by its tag id.
		assertNotNull(deviceTagService.findByTagId(testHelper.getTagId("tag1")));

		// Assert non-existing tag id can't find any device.
		assertTrue(deviceTagService.findByTagId("nonExistentTag").isEmpty());
	}

	@Test
	void countByTag() {
		// Perform a save operation for a new device.
		deviceService.saveNew(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				testHelper.getTagId("tag1"),
				CORE)).getId().toHexString();

		// Assert existing tags count one device.
		assertEquals(1, deviceTagService.countByTag(List.of("tag1"), false));
		assertEquals(1, deviceTagService.countByTag(List.of("tag1"), true));
		assertEquals(1, deviceTagService.countByTag(List.of("tag"), true));

		// Assert non-existing tags can't count any devices.
		assertEquals(0, deviceTagService.countByTag(List.of("nonExistentTag"), false));
		assertEquals(0, deviceTagService.countByTag(List.of("nonExistentTag"), true));
	}

	@Test
	void removeTagById() {

		// Perform a save operation for a new device.
		String deviceID = deviceService.saveNew(
			testHelper.makeDeviceEntity(
				"test-hardware-id",
				AppConstants.Device.Status.REGISTERED,
				"tag1",
				CORE)).getId().toHexString();

		//Assert no tag is removed when tag does not exist.
		assertDoesNotThrow(() -> deviceTagService.removeTagById("nonExistentTag"));
		assertFalse(deviceService.findById(deviceID).getTags().isEmpty());

		// Assert tag is removed when tag exists.
		assertDoesNotThrow(() -> deviceTagService.removeTagById("tag1"));
		assertTrue(deviceService.findById(deviceID).getTags().isEmpty());

	}
}
