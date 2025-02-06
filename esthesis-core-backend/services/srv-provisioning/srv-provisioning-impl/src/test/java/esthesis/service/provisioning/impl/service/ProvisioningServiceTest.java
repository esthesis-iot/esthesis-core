package esthesis.service.provisioning.impl.service;

import static esthesis.core.common.AppConstants.Provisioning.Type.EXTERNAL;
import static esthesis.core.common.AppConstants.Provisioning.Type.INTERNAL;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import esthesis.common.exception.QMismatchException;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.common.gridfs.GridFSDTO;
import esthesis.service.common.gridfs.GridFSService;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.TestHelper;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
@QuarkusTest
class ProvisioningServiceTest {

	@Inject
	ProvisioningService provisioningService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsResource settingsResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	DeviceResource deviceResource;

	@Inject
	TestHelper testHelper;

	@InjectMock
	GridFSService gridFSService;


	@SneakyThrows
	@BeforeEach
	void setUp() {
		// Clear the test database before each test.
		testHelper.clearDatabase();

		// Mock semantic versioning as enabled.
		when(settingsResource.findByName(NamedSetting.DEVICE_PROVISIONING_SEMVER))
			.thenReturn(new SettingEntity(NamedSetting.DEVICE_PROVISIONING_SEMVER.name(), "true"));

		// Mock GridFS operations save, delete and download.
		when(gridFSService.saveBinary(any(GridFSDTO.class))).thenReturn(new ObjectId());
		doNothing().when(gridFSService).deleteBinary(any(GridFSDTO.class));
		when(gridFSService.downloadBinary(any(GridFSDTO.class))).thenReturn(
			Uni.createFrom().item(new byte[0]));

	}

	@SneakyThrows
	@Test
	void saveNew() {
		// Perform a save of a new provisioning package.
		String id = provisioningService.saveNew(
			testHelper.createProvisioningPackageEntity(
				"test-provisioning-package",
				"1.0.1",
				"1.0.0",
				true,
				List.of("test-tag"),
				INTERNAL),
			testHelper.createFileUpload()).getId().toHexString();

		// Assert entity was persisted.
		assertNotNull(provisioningService.findById(id));

		// Assert GridFS save operation was called.
		verify(gridFSService, times(1)).saveBinary(any(GridFSDTO.class));

	}

	@SneakyThrows
	@Test
	void saveUpdate() {
		// Perform a save of a new provisioning package.
		ProvisioningPackageEntity provisioningPackage = provisioningService.saveNew(
			testHelper.createProvisioningPackageEntity(
				"test-provisioning-package",
				"1.0.1",
				"1.0.0",
				true,
				List.of("test-tag"),
				INTERNAL),
			testHelper.createFileUpload());

		// Get the id of the saved entity.
		String id = provisioningPackage.getId().toHexString();

		// Update values and perform an update operation.
		provisioningPackage.setName("updated-provisioning-package");
		provisioningPackage.setVersion("1.0.2");
		provisioningService.saveUpdate(provisioningPackage, testHelper.createFileUpload());

		// Assert entity was updated.
		ProvisioningPackageEntity updatedProvisioningPackage = provisioningService.findById(id);

		assertEquals("updated-provisioning-package", updatedProvisioningPackage.getName());
		assertEquals("1.0.2", updatedProvisioningPackage.getVersion());

		// Assert GridFS save operation was called twice.
		verify(gridFSService, times(2)).saveBinary(any(GridFSDTO.class));
	}

	@Test
	void delete() {
		// Perform a save of a new provisioning package.
		String id = provisioningService.saveNew(
			testHelper.createProvisioningPackageEntity(
				"test-provisioning-package",
				"1.0.1",
				"1.0.0",
				true,
				List.of("test-tag"),
				INTERNAL),
			testHelper.createFileUpload()).getId().toHexString();

		// Confirm the entity was persisted.
		assertNotNull(provisioningService.findById(id));

		// Delete the entity.
		provisioningService.delete(id);

		// Confirm the entity was deleted.
		assertNull(provisioningService.findById(id));

		// Assert GridFS delete operation was called.
		verify(gridFSService, times(1)).deleteBinary(any(GridFSDTO.class));

	}


	@Test
	void download() {
		// Perform a save of an internal provisioning package.
		String internalProvisioningPackageId =
			provisioningService.saveNew(
				testHelper.createProvisioningPackageEntity(
					"internal-provisioning-package",
					"1.0.1",
					"1.0.0",
					true,
					List.of("test-tag"),
					INTERNAL),
				testHelper.createFileUpload()).getId().toHexString();

		// Perform a save of an external provisioning package.
		String externalProvisioningPackageId =
			provisioningService.saveNew(
				testHelper.createProvisioningPackageEntity(
					"exteral-provisioning-package",
					"1.0.1",
					"1.0.0",
					true,
					List.of("test-tag"),
					EXTERNAL),
				testHelper.createFileUpload()).getId().toHexString();

		// Assert internal provisioning packages download does not throw any exception.
		assertDoesNotThrow(() -> provisioningService.download(internalProvisioningPackageId));

		// Assert external provisioning packages download throws a QMismatchException.
		assertThrows(QMismatchException.class,
			() -> provisioningService.download(externalProvisioningPackageId));

		// Assert GridFS download operation was called only once.
		verify(gridFSService, times(1)).downloadBinary(any(GridFSDTO.class));
	}

	@Test
	void findByTags() {
		// Perform a save of a provisioning package with a test-tag.
		provisioningService.saveNew(
			testHelper.createProvisioningPackageEntity(
				"test-provisioning-package",
				"1.0.1",
				"1.0.0",
				true,
				List.of("test-tag"),
				INTERNAL),
			testHelper.createFileUpload());

		// Assert a non-existent tag won't bring results.
		assertTrue(provisioningService.findByTags("non-existing").isEmpty());

		// Assert a valid tag will find the provisioning package.
		assertEquals(1, provisioningService.findByTags("test-tag").size());

		// Todo cover multiple tags and SemServer disabled options
	}

	@Test
	void semVerFind() {
		// Mock finding a valid device with a test-tag.
		when(deviceResource.findByHardwareIds(anyString()))
			.thenReturn(List.of(testHelper.createDeviceEntity("testHardwareId", List.of("test-tag"))));

		// Perform a save of a provisioning package with semver version 2.1.1 without prerequisite version.
		provisioningService.saveNew(testHelper.createProvisioningPackageEntity(
			"test-provisioning-package",
			"2.1.1",
			null,
			true,
			List.of("test-tag"),
			INTERNAL), testHelper.createFileUpload());

		// Assert a candidate semver version is found for the given hardware id and smaller current versions.
		assertNotNull(provisioningService.semVerFind("testHardwareId", "1.0.0"));
		assertNotNull(provisioningService.semVerFind("testHardwareId", "2.0.0"));
		assertNotNull(provisioningService.semVerFind("testHardwareId", "2.0.1"));
		assertNotNull(provisioningService.semVerFind("testHardwareId", "2.1.0"));

		// Assert a candidate semver version is not found for the given hardware id and equals or higher current versions.
		assertThrows(Exception.class, () -> provisioningService.semVerFind("testHardwareId", "2.1.1"));
		assertThrows(Exception.class, () -> provisioningService.semVerFind("testHardwareId", "2.1.2"));
		assertThrows(Exception.class, () -> provisioningService.semVerFind("testHardwareId", "2.2.1"));
		assertThrows(Exception.class, () -> provisioningService.semVerFind("testHardwareId", "3.0.0"));

		// Todo test for semVerFind with prerequisite version.
	}

	@Test
	void find() {
		// Assert no provisioning packages are returned.
		assertEquals(0,
			provisioningService.find(testHelper.makePageable(0, 10)).getContent().size());

		// Perform a save of a provisioning package.
		provisioningService.saveNew(
			testHelper.createProvisioningPackageEntity(
				"test-provisioning-package",
				"1.0.1",
				"1.0.0",
				true,
				List.of("test-tag"),
				INTERNAL),
			testHelper.createFileUpload());

		// Assert one provisioning package is returned.
		assertEquals(1,
			provisioningService.find(testHelper.makePageable(0, 10)).getContent().size());

		// Todo cover more cases with different parameters.

	}

	@Test
	void findById() {
		// Assert no entity is found for non-existent id.
		assertNull(provisioningService.findById(new ObjectId().toHexString()));

		// Perform a save of a provisioning package.
		String id = provisioningService.saveNew(
			testHelper.createProvisioningPackageEntity(
				"test-provisioning-package",
				"1.0.1",
				"1.0.0",
				true,
				List.of("test-tag"),
				INTERNAL),
			testHelper.createFileUpload()).getId().toHexString();

		// Assert the entity was found.
		assertNotNull(provisioningService.findById(id));
	}
}
