package esthesis.service.crypto.impl.service;

import esthesis.service.crypto.entity.KeystoreEntity;
import esthesis.service.crypto.impl.TestHelper;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
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

import java.util.List;

import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class KeystoreServiceTest {

	@Inject
	TestHelper testHelper;

	@Inject
	KeystoreService keystoreService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsResource settingsResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	DeviceResource deviceResource;

	int initialKeystoreSizeInDB = 0;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		testHelper.createEntities(null);
		testHelper.createEntities(testHelper.findOneCaEntity());

		initialKeystoreSizeInDB = testHelper.findAllKeystoreEntity().size();

		log.info("Initial keystore size in DB: {}", initialKeystoreSizeInDB);

		// Mock the relevant settings
		SettingEntity mockKeyAlgorithmSetting = mock(SettingEntity.class);
		SettingEntity mockKeySizeSetting = mock(SettingEntity.class);
		SettingEntity mockSignatureAlgorithmSetting = mock(SettingEntity.class);

		when(mockKeyAlgorithmSetting.asString()).thenReturn("RSA"); // Valid asymmetric key algorithm
		when(mockKeySizeSetting.asInt()).thenReturn(2048); // Typical key size for RSA
		when(mockSignatureAlgorithmSetting.asString()).thenReturn("SHA256withRSA"); // Common signature algorithm

		when(settingsResource.findByName(SECURITY_ASYMMETRIC_KEY_ALGORITHM))
			.thenReturn(mockKeyAlgorithmSetting);
		when(settingsResource.findByName(SECURITY_ASYMMETRIC_KEY_SIZE))
			.thenReturn(mockKeySizeSetting);
		when(settingsResource.findByName(SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM))
			.thenReturn(mockSignatureAlgorithmSetting);

		// Mock the relevant device settings
		when(deviceResource.get(anyString())).thenReturn(testHelper.makeDeviceEntity("test-device-1"));
		when(deviceResource.findByTagName(anyString())).thenReturn(
			List.of(
				testHelper.makeDeviceEntity("test-device-1"),
				testHelper.makeDeviceEntity("test-device-2"))
		);
	}

	@SneakyThrows
	@Test
	void download() {
		// Arrange
		KeystoreEntity keystore = testHelper.findOneKeystoreEntity();
		String validKeystoreId = keystore.getId().toString();

		// Act
		byte[] keystoreBytes = keystoreService.download(validKeystoreId);

		// Assert
		assertNotNull(keystoreBytes);
	}

	@Test
	void findById() {
		// Arrange
		String validKeystoreId = testHelper.findOneKeystoreEntity().getId().toString();
		String unexistingKeystoreId = new ObjectId().toString();

		// Act
		KeystoreEntity unexistingKeystoreEntity = keystoreService.findById(unexistingKeystoreId);
		KeystoreEntity keystoreEntity = keystoreService.findById(validKeystoreId);

		// Assert
		assertNull(unexistingKeystoreEntity);
		assertNotNull(keystoreEntity);
	}

	@Test
	void find() {

		// Act
		List<KeystoreEntity> keystoreEntities =
			keystoreService.find(testHelper.makePageable(0, 100), true).getContent();

		// Assert
		assertEquals(initialKeystoreSizeInDB, keystoreEntities.size());

	}

	@Test
	void saveNew() {
		KeystoreEntity keystoreEntity = new KeystoreEntity();
		keystoreEntity.setName("test-keystore");
		keystoreEntity.setType("PKCS12");
		keystoreEntity.setDescription("Test keystore");
		keystoreEntity.setPassword("test-password");
		keystoreEntity.setVersion(1);

		// Act
		KeystoreEntity savedKeystoreEntity = keystoreService.saveNew(keystoreEntity);

		// Assert new keystore was created
		assertEquals(initialKeystoreSizeInDB + 1, testHelper.findAllKeystoreEntity().size());

		// Assert saved keystore values are correct
		assertNotNull(savedKeystoreEntity);
		assertEquals("test-keystore", savedKeystoreEntity.getName());
		assertEquals("PKCS12", savedKeystoreEntity.getType());
		assertEquals("Test keystore", savedKeystoreEntity.getDescription());
		assertEquals("test-password", savedKeystoreEntity.getPassword());
		assertEquals(1, savedKeystoreEntity.getVersion());

	}

	@Test
	void saveUpdate() {
		// Arrange
		KeystoreEntity existingKeystore = testHelper.findOneKeystoreEntity();
		existingKeystore.setName("updated-keystore");
		existingKeystore.setVersion(2);
		existingKeystore.setDescription("Updated description");

		// Act
		keystoreService.saveUpdate(existingKeystore);

		// Arrange
		KeystoreEntity updatedKeystore = testHelper.findOneKeystoreEntityById(existingKeystore.getId().toString());

		// Assert entity was updated
		assertNotNull(updatedKeystore);
		assertEquals("updated-keystore", updatedKeystore.getName());
		assertEquals(2, updatedKeystore.getVersion());
		assertEquals("Updated description", updatedKeystore.getDescription());

		// Assert no new keystore was created
		assertEquals(initialKeystoreSizeInDB, testHelper.findAllKeystoreEntity().size());

	}

	@Test
	void deleteById() {
		// Arrange
		String validKeystoreId = testHelper.findOneKeystoreEntity().getId().toString();
		String unexistentKeystoreId = new ObjectId().toString();

		// Act try to delete an unexistent keystore
		keystoreService.deleteById(unexistentKeystoreId);

		// Assert nothing happens
		assertEquals(initialKeystoreSizeInDB, testHelper.findAllKeystoreEntity().size());

		// Act try to delete a valid keystore
		keystoreService.deleteById(validKeystoreId);

		// Assert the keystore is deleted
		assertEquals(initialKeystoreSizeInDB - 1, testHelper.findAllKeystoreEntity().size());
	}
}
