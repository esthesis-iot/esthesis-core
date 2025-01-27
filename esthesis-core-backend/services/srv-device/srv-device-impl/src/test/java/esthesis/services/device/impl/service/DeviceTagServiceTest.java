package esthesis.services.device.impl.service;

import esthesis.service.device.entity.DeviceEntity;
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

import static org.junit.jupiter.api.Assertions.*;
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

	int initialDeviceSizeInDB = 0;

	@BeforeEach
	void setUp() {
		testHelper.setup();
		testHelper.createEntities();

		// Mock the tag resource requests
		when(tagResource.findByNames(eq("tag1"), anyBoolean())).thenReturn(List.of(testHelper.makeTag("tag1")));
		when(tagResource.findByNames(eq("tag2"), anyBoolean())).thenReturn(List.of(testHelper.makeTag("tag2")));
		when(tagResource.findByNames(eq("tag3"), anyBoolean())).thenReturn(List.of(testHelper.makeTag("tag3")));
		when(tagResource.findByNames(eq("tag4"), anyBoolean())).thenReturn(List.of(testHelper.makeTag("tag4")));
		when(tagResource.findByNames(eq("tag5"), anyBoolean())).thenReturn(List.of(testHelper.makeTag("tag5")));
		when(tagResource.findByNames(eq("tag6"), anyBoolean())).thenReturn(List.of(testHelper.makeTag("tag6")));
		when(tagResource.findByNames(eq("tag"), anyBoolean())).thenReturn(List.of(
			testHelper.makeTag("tag1"),
			testHelper.makeTag("tag2"),
			testHelper.makeTag("tag3"),
			testHelper.makeTag("tag4"),
			testHelper.makeTag("tag5"),
			testHelper.makeTag("tag6"))
		);

		initialDeviceSizeInDB = testHelper.findAllDeviceEntity().size();
		log.info("Initial device size in DB: {}", initialDeviceSizeInDB);
	}

	@Test
	void findByTagName() {
		// Arrange
		String existentTagName = "tag1";
		String existentPartialTagName = "tag";
		String nonExistentTagName = "nonExistentTag";

		// Act
		List<DeviceEntity> existentTagDevicesExactMatch =
			deviceTagService.findByTagName(existentTagName, false);
		List<DeviceEntity> existentTagDevicesPartialMatch =
			deviceTagService.findByTagName(existentPartialTagName, true);

		List<DeviceEntity> nonExistentTagDevicesExactMatch =
			deviceTagService.findByTagName(nonExistentTagName, false);
		List<DeviceEntity> nonExistentTagDevicesPartiaMatch =
			deviceTagService.findByTagName(nonExistentTagName, true);

		// Assert
		assertEquals(2, existentTagDevicesExactMatch.size());
		assertEquals(8, existentTagDevicesPartialMatch.size());
		assertTrue(nonExistentTagDevicesExactMatch.isEmpty());
		assertTrue(nonExistentTagDevicesPartiaMatch.isEmpty());

	}

	@Test
	void findByTagNameList() {
		// Arrange
		String existentTagName = "tag1";
		String existentPartialTagName = "tag";
		String nonExistentTagName = "nonExistentTag";

		// Act
		List<DeviceEntity> existentTagDevicesExactMatch =
			deviceTagService.findByTagName(List.of(existentTagName), false);
		List<DeviceEntity> existentTagDevicesPartialMatch =
			deviceTagService.findByTagName(List.of(existentPartialTagName), true);

		List<DeviceEntity> nonExistentTagDevicesExactMatch =
			deviceTagService.findByTagName(List.of(nonExistentTagName), false);
		List<DeviceEntity> nonExistentTagDevicesPartiaMatch =
			deviceTagService.findByTagName(List.of(nonExistentTagName), true);

		// Assert
		assertEquals(2, existentTagDevicesExactMatch.size());
		assertEquals(8, existentTagDevicesPartialMatch.size());
		assertTrue(nonExistentTagDevicesExactMatch.isEmpty());
		assertTrue(nonExistentTagDevicesPartiaMatch.isEmpty());
	}

	@Test
	void findByTagId() {
		// Arrange
		String existentTagId = testHelper.getTagId("tag1");
		String nonExistentTagId = "nonExistentTag";

		// Act
		List<DeviceEntity> existentTagDevices = deviceTagService.findByTagId(existentTagId);
		List<DeviceEntity> nonExistentTagDevices = deviceTagService.findByTagId(nonExistentTagId);

		// Assert
		assertEquals(2, existentTagDevices.size());
		assertTrue(nonExistentTagDevices.isEmpty());
	}

	@Test
	void countByTag() {
		// Arrange
		String existentTagName = "tag1";
		String existentPartialTagName = "tag";
		String nonExistentTagName = "nonExistentTag";

		// Act
		Long existentTagDevices = deviceTagService.countByTag(List.of(existentTagName), false);
		Long existentTagDevicesPartial = deviceTagService.countByTag(List.of(existentPartialTagName), true);
		Long nonExistentTagDevices = deviceTagService.countByTag(List.of(nonExistentTagName), false);
		Long nonExistentTagDevicesPartial = deviceTagService.countByTag(List.of(nonExistentTagName), true);

		// Assert
		assertEquals(2L, existentTagDevices);
		assertEquals(initialDeviceSizeInDB, existentTagDevicesPartial);
		assertEquals(0L, nonExistentTagDevices);
		assertEquals(0L, nonExistentTagDevicesPartial);
	}

	@Test
	void removeTagById() {
		// Arrange
		String existentTagId = testHelper.getTagId("tag1");
		String nonExistentTagId = "nonExistentTag";

		// Act & Assert - No exception is thrown and no tag is removed
		assertDoesNotThrow(() -> deviceTagService.removeTagById(nonExistentTagId));
		List<DeviceEntity> devices = testHelper.findAllDeviceEntity();
		assertTrue(devices.stream().noneMatch(device -> device.getTags().isEmpty()));

		// Act & Assert - tag was removed
		deviceTagService.removeTagById(existentTagId);
		devices = testHelper.findAllDeviceEntity();
		assertTrue(devices.stream().noneMatch(device -> device.getTags().contains(existentTagId)));

	}
}
