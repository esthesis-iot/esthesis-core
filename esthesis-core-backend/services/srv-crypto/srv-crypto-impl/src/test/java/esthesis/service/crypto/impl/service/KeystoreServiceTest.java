package esthesis.service.crypto.impl.service;

import esthesis.service.crypto.entity.CaEntity;
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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

	@Inject
	CAService caService;

	@Inject
	CertificateService certificateService;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Mock the security-related settings.
		SettingEntity mockKeyAlgorithmSetting = mock(SettingEntity.class);
		SettingEntity mockKeySizeSetting = mock(SettingEntity.class);
		SettingEntity mockSignatureAlgorithmSetting = mock(SettingEntity.class);

		when(mockKeyAlgorithmSetting.asString()).thenReturn("RSA");
		when(mockKeySizeSetting.asInt()).thenReturn(2048);
		when(mockSignatureAlgorithmSetting.asString()).thenReturn("SHA256withRSA");

		when(settingsResource.findByName(SECURITY_ASYMMETRIC_KEY_ALGORITHM))
			.thenReturn(mockKeyAlgorithmSetting);
		when(settingsResource.findByName(SECURITY_ASYMMETRIC_KEY_SIZE))
			.thenReturn(mockKeySizeSetting);
		when(settingsResource.findByName(SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM))
			.thenReturn(mockSignatureAlgorithmSetting);

		// Mock getting and finding devices by tag name.
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
		// Perform a save operation for a new CA, certificate and keystore.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		String certificateId = certificateService.save(testHelper.makeCertificateEntity(ca)).getId().toHexString();

		String keystoreId =
			keystoreService.saveNew(
					testHelper.makeKeystoreEntity(certificateId, ca.getId().toHexString()))
				.getId()
				.toHexString();


		// Assert keystore can be downloaded.
		assertNotNull(keystoreService.download(keystoreId));
	}

	@Test
	void findById() {
		// Perform a save operation for a new CA, certificate and keystore.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		String certificateId = certificateService.save(testHelper.makeCertificateEntity(ca)).getId().toHexString();

		String keystoreId =
			keystoreService.saveNew(
					testHelper.makeKeystoreEntity(certificateId, ca.getId().toHexString()))
				.getId()
				.toHexString();

		// Assert keystore can be found by id.
		assertNotNull(keystoreService.findById(keystoreId));
	}

	@Test
	void find() {
		// Assert no keystores are found.
		assertTrue(keystoreService.find(testHelper.makePageable(0, 100), true).getContent().isEmpty());

		// Perform a save operation for a new CA, certificate and keystore.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		String certificateId = certificateService.save(testHelper.makeCertificateEntity(ca)).getId().toHexString();

		keystoreService.saveNew(testHelper.makeKeystoreEntity(certificateId, ca.getId().toHexString()));

		// Assert keystore can be found.
		assertFalse(keystoreService.find(testHelper.makePageable(0, 100), true).getContent().isEmpty());

	}

	@Test
	void saveNew() {

		// Prepare a new keystore entity to be saved.
		KeystoreEntity keystoreEntity = new KeystoreEntity();
		keystoreEntity.setName("test-keystore");
		keystoreEntity.setType("PKCS12");
		keystoreEntity.setDescription("Test keystore");
		keystoreEntity.setPassword("test-password");
		keystoreEntity.setVersion(1);

		// Perform the save operation.
		String keystoreId = keystoreService.saveNew(keystoreEntity).getId().toHexString();


		// Assert keystore was saved with correct values.
		KeystoreEntity keystore = keystoreService.findById(keystoreId);

		assertEquals("test-keystore", keystore.getName());
		assertEquals("PKCS12", keystore.getType());
		assertEquals("Test keystore", keystore.getDescription());
		assertEquals("test-password", keystore.getPassword());
		assertEquals(1, keystore.getVersion());

	}

	@Test
	void saveUpdate() {
		// Perform a save operation for a new CA, certificate and keystore.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		String certificateId = certificateService.save(testHelper.makeCertificateEntity(ca)).getId().toHexString();

		String keystoreId =
			keystoreService.saveNew(
					testHelper.makeKeystoreEntity(certificateId, ca.getId().toHexString()))
				.getId()
				.toHexString();

		// Perform an update operation.
		KeystoreEntity keystore = keystoreService.findById(keystoreId);
		keystore.setName("updated-keystore");
		keystore.setVersion(2);
		keystore.setDescription("Updated description");
		keystoreService.saveUpdate(keystore);

		// Assert keystore was updated with correct values.
		KeystoreEntity updatedKeystore = keystoreService.findById(keystoreId);
		assertEquals("updated-keystore", updatedKeystore.getName());
		assertEquals(2, updatedKeystore.getVersion());
		assertEquals("Updated description", updatedKeystore.getDescription());
	}

	@Test
	void deleteById() {
		// Perform a save operation for a new CA, certificate and keystore.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		String certificateId = certificateService.save(testHelper.makeCertificateEntity(ca)).getId().toHexString();

		String keystoreId =
			keystoreService.saveNew(
					testHelper.makeKeystoreEntity(certificateId, ca.getId().toHexString()))
				.getId()
				.toHexString();

		// Assert keystore exists.
		assertNotNull(keystoreService.findById(keystoreId));

		// Perform the delete operation for the keystore.
		keystoreService.deleteById(keystoreId);

		// Assert keystore was deleted.
		assertNull(keystoreService.findById(keystoreId));
	}
}
