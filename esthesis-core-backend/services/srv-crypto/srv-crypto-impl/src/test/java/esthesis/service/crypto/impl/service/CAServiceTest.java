package esthesis.service.crypto.impl.service;

import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.impl.TestHelper;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.server.core.multipart.DefaultFileUpload;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class CAServiceTest {

	@Inject
	TestHelper testHelper;

	@Inject
	CAService caService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsResource settingsResource;


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

	}

	@Test
	void save() {
		// Perform the operation for saving a new CA.
		String newCaId = caService.save(new CaEntity()
				.setCn("test-new-ca")
				.setIssued(Instant.now())
				.setValidity(Instant.now().plus(360, ChronoUnit.DAYS))
				.setName("Test new CA")
				.setPrivateKey("test-private-key")
				.setPublicKey("test-public-key"))
			.getId()
			.toHexString();

		// Assert CA was saved with the provided values.
		CaEntity savedCa = caService.findById(newCaId);
		assertEquals("test-new-ca", savedCa.getCn());
		assertNotNull(savedCa.getIssued());
		assertNotNull(savedCa.getValidity());
		assertEquals("Test new CA", savedCa.getName());
		assertNotNull(savedCa.getPrivateKey());
		assertNotNull(savedCa.getPublicKey());


	}

	@Test
	void getEligibleForSigning() {
		// Assert that no CAs are eligible for signing.
		assertTrue(caService.getEligibleForSigning().isEmpty());

		// Perform the operation for saving a new eligible CA.
		caService.save(testHelper.makeCaEntity(null));

		// Assert CA eligible for signing exists.
		assertFalse(caService.getEligibleForSigning().isEmpty());
	}

	@SneakyThrows
	@Test
	void importCa() {

		// Load files from the test resources folder to be used as form values.
		Path publicKeyPath =
			Paths.get(Objects.requireNonNull(
					getClass().getClassLoader().getResource("public-key-test.txt"))
				.toURI());

		Path privateKeyPath =
			Paths.get(Objects.requireNonNull(
					getClass().getClassLoader().getResource("private-key-test.txt"))
				.toURI());

		Path certificatePath =
			Paths.get(Objects.requireNonNull(
					getClass().getClassLoader().getResource("certificate-test.txt"))
				.toURI());

		// Create FormValue mocks for each file.
		FormValue publicKeyFormValue = mock(FormValue.class);
		when(publicKeyFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(publicKeyPath));

		FormValue privateKeyFormValue = mock(FormValue.class);
		when(privateKeyFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(privateKeyPath));

		FormValue certificateFormValue = mock(FormValue.class);
		when(certificateFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(certificatePath));

		// Create DefaultFileUpload instances for each file.
		DefaultFileUpload publicKey = new DefaultFileUpload("Test Public Key", publicKeyFormValue);
		DefaultFileUpload privateKey = new DefaultFileUpload("Test Private Key", privateKeyFormValue);
		DefaultFileUpload certificate = new DefaultFileUpload("Test Certificate", certificateFormValue);

		// Prepare CA to be imported.
		CaEntity importedCaEntity = new CaEntity();
		importedCaEntity.setCertificate("Test Certificate");
		importedCaEntity.setCn("Test Cn");
		importedCaEntity.setIssued(Instant.now());
		importedCaEntity.setName("Test Name");
		importedCaEntity.setPrivateKey("Test Private Key");
		importedCaEntity.setPublicKey("Test Public Key");


		// Perform the operation for importing a CA with the given mocked files.
		String caId =
			caService.importCa(importedCaEntity, publicKey, privateKey, certificate).getId().toHexString();

		// Assert CA was imported and saved with the provided files.
		CaEntity caEntity = caService.findById(caId);
		assertNotNull(caEntity);
		assertEquals("Test Name", caEntity.getName());
		assertNotNull(caEntity.getPublicKey());
		assertNotNull(caEntity.getPrivateKey());
		assertNotNull(caEntity.getCertificate());
	}

	@Test
	void getPrivateKey() {
		// Perform the operation for saving a CA with a private key.
		String caId = caService.save(testHelper.makeCaEntity(null)).getId().toHexString();

		// Assert private key was saved.
		assertNotNull(caService.getPrivateKey(caId));
	}

	@Test
	void getPublicKey() {
		// Perform the operation for saving a CA with a public key.
		String caId = caService.save(testHelper.makeCaEntity(null)).getId().toHexString();

		// Assert public key was saved.
		assertNotNull(caService.getPublicKey(caId));
	}

	@Test
	void getCertificate() {
		// Perform the operation for saving a CA with a certificate.
		String caId = caService.save(testHelper.makeCaEntity(null)).getId().toHexString();

		// Assert certificate was saved.
		assertNotNull(caService.getCertificate(caId));
	}

	@Test
	void deleteById() {
		// Perform the operation for saving a new CA.
		String caId = caService.save(testHelper.makeCaEntity(null)).getId().toHexString();

		// Assert CA is persisted.
		assertNotNull(caService.findById(caId));

		// Perform the operation for deleting the CA.
		caService.deleteById(caId);

		// Assert CA was deleted.
		assertNull(caService.findById(caId));

	}

	@Test
	void findFirstByColumn() {
		// Perform the operation for saving a new CA.
		caService.save(new CaEntity()
			.setCn("test cn")
			.setName("test name")
			.setIssued(Instant.now())
			.setValidity(Instant.now().plus(360, ChronoUnit.DAYS)));

		// Assert CA can be found by valid columns values.
		assertNotNull(caService.findFirstByColumn("name", "test name"));
		assertNotNull(caService.findFirstByColumn("cn", "test cn"));

		// Assert CA cannot be found by invalid columns values.
		assertNull(caService.findFirstByColumn("name", "invalid name"));
		assertNull(caService.findFirstByColumn("cn", "invalid cn"));
	}

	@Test
	void findById() {
		// Perform the operation for saving a new CA.
		String caId = caService.save(testHelper.makeCaEntity(null)).getId().toHexString();

		// Assert CA can be found by id.
		assertNotNull(caService.findById(caId));
	}

	@Test
	void find() {
		// Assert no CAs are found.
		assertTrue(caService.find(testHelper.makePageable(0, 100), true).getContent().isEmpty());

		// Perform the operation for saving a new CA.
		caService.save(testHelper.makeCaEntity(null));

		// Assert CA can be found.
		assertFalse(caService.find(testHelper.makePageable(0, 100), true).getContent().isEmpty());
	}
}
