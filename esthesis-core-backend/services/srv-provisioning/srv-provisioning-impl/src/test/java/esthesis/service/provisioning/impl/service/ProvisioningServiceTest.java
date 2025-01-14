package esthesis.service.provisioning.impl.service;

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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.core.common.AppConstants.Provisioning.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

	int initialProvisioningPackageSizeInDB = 0;
	int initialInternalProvisioningPackageSizeInDB = 0;
	int inititalExternalProvisioningPackageSizeInDB = 0;


	@SneakyThrows
	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		testHelper.createProvisioningPackages();

		initialProvisioningPackageSizeInDB = testHelper.findAllProvisioningPackages().size();
		initialInternalProvisioningPackageSizeInDB = testHelper.findAllInternalProvisioningPackages().size();
		inititalExternalProvisioningPackageSizeInDB = testHelper.findAllExternalProvisioningPackages().size();
		log.info("Initial provisioning package size in DB: {}", initialProvisioningPackageSizeInDB);
		log.info("Initial internal provisioning package size in DB: {}", initialInternalProvisioningPackageSizeInDB);
		log.info("Initial external provisioning package size in DB: {}", inititalExternalProvisioningPackageSizeInDB);

		when(settingsResource.findByName(NamedSetting.DEVICE_PROVISIONING_SEMVER))
			.thenReturn(new SettingEntity(NamedSetting.DEVICE_PROVISIONING_SEMVER.name(), "true"));

		when(deviceResource.findByHardwareIds(anyString(), anyBoolean()))
			.thenReturn(List.of(testHelper.makeDeviceEntity("testHardwareId")));

		when(gridFSService.saveBinary(any(GridFSDTO.class))).thenReturn(new ObjectId());
		doNothing().when(gridFSService).deleteBinary(any(GridFSDTO.class));
		when(gridFSService.downloadBinary(any(GridFSDTO.class))).thenReturn(Uni.createFrom().item(new byte[0]));

	}

	@SneakyThrows
	@Test
	void saveNew() {
		// Arrange
		ProvisioningPackageEntity provisioningPackage =
			testHelper.createInternalProvisioningPackage("test-provisioning-package", "1.0.1");
		FileUpload fileUpload = testHelper.createFileUpload();

		// Act
		ProvisioningPackageEntity newProvisioningPackage = provisioningService.saveNew(provisioningPackage, fileUpload);

		// Assert
		ProvisioningPackageEntity savedProvisioningPackage =
			testHelper.findProvisioningPackage(newProvisioningPackage.getId().toString());


		assertNotNull(savedProvisioningPackage);
		assertEquals("test-provisioning-package", savedProvisioningPackage.getName());
		assertEquals("1.0.1", savedProvisioningPackage.getVersion());
		assertEquals(
			initialProvisioningPackageSizeInDB + 1,
			testHelper.findAllProvisioningPackages().size(),
			"Database size should increase by 1 after saving a new provisioning package.");

		verify(gridFSService, times(1)).saveBinary(any(GridFSDTO.class));

	}

	@SneakyThrows
	@Test
	void saveUpdate() {
		// Arrange
		ProvisioningPackageEntity exisingProvisioningPackage = testHelper.findOneProvisioningPackage(Type.INTERNAL);
		exisingProvisioningPackage.setName("test-provisioning-package-updated");
		exisingProvisioningPackage.setVersion("1.0.2");
		exisingProvisioningPackage.setAvailable(true);
		FileUpload fileUpload = testHelper.createFileUpload();

		// Act
		provisioningService.saveUpdate(exisingProvisioningPackage, fileUpload);

		// Assert
		ProvisioningPackageEntity updatedProvisioningPackage =
			testHelper.findProvisioningPackage(exisingProvisioningPackage.getId().toString());

		assertEquals("test-provisioning-package-updated", updatedProvisioningPackage.getName());
		assertEquals("1.0.2", updatedProvisioningPackage.getVersion());
		assertEquals(initialProvisioningPackageSizeInDB,
			testHelper.findAllProvisioningPackages().size(),
			"Database size should not change after updating a package.");

		verify(gridFSService, times(1)).saveBinary(any(GridFSDTO.class));
	}

	@Test
	void delete() {
		// Arrange
		List<String> provisioningPackageIds =
			testHelper.findAllProvisioningPackages()
				.stream()
				.map(ProvisioningPackageEntity::getId)
				.map(Object::toString).toList();

		// Act
		for (String id : provisioningPackageIds) {
			provisioningService.delete(id);
		}

		// Assert
		assertEquals(0,
			testHelper.findAllProvisioningPackages().size(),
			"Database size should be 0 after deleting all packages.");

		verify(gridFSService, times(initialInternalProvisioningPackageSizeInDB)).deleteBinary(any(GridFSDTO.class));

	}


	@Test
	void download() {
		// Arrange
		ProvisioningPackageEntity internalProvisioningPackage = testHelper.findOneProvisioningPackage(Type.INTERNAL);
		ProvisioningPackageEntity externalProvisioningPackage = testHelper.findOneProvisioningPackage(Type.EXTERNAL);
		String internalProvisioningPackageId = internalProvisioningPackage.getId().toString();
		String externalProvisioningPackageId = externalProvisioningPackage.getId().toString();

		// Act && Assert
		assertDoesNotThrow(
			() -> provisioningService.download(internalProvisioningPackageId),
			"Internal provisioning packages download should not throw any exception."
		);

		assertThrows(
			QMismatchException.class,
			() -> provisioningService.download(externalProvisioningPackageId),
			"External provisioning packages download should throw a QMismatchException.");

		verify(gridFSService, times(1)).downloadBinary(any(GridFSDTO.class));
	}

	@Test
	void findByTags() {
		// ToDo
	}

	@Test
	void semVerFind() {
		// ToDo
	}

	@Test
	void find() {
		// ToDo
	}

	@Test
	void findById() {
		// ToDo
	}
}
