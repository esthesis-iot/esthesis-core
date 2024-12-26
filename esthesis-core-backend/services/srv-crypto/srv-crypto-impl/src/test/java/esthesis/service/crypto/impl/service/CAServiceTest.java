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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

	int initialCaSizeInDB = 0;
	int initialCertificateSizeInDB = 0;
	int initialKeystoreSizeInDB = 0;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		testHelper.createEntities(null);
		testHelper.createEntities(testHelper.findOneCaEntity());

		initialCaSizeInDB = testHelper.findAllCaEntity().size();
		initialCertificateSizeInDB = testHelper.findAllCertificateEntity().size();
		initialKeystoreSizeInDB = testHelper.findAllKeystoreEntity().size();

		log.info("Initial CA size in DB: {}", initialCaSizeInDB);
		log.info("Initial certificate size in DB: {}", initialCertificateSizeInDB);
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

	}

	@Test
	void save() {
		CaEntity newCa = new CaEntity();
		newCa.setCn("test-new-ca");
		newCa.setIssued(Instant.now());
		newCa.setValidity(Instant.now().plus(360, ChronoUnit.DAYS));
		newCa.setName("Test new CA");
		newCa.setPrivateKey("test-private-key");
		newCa.setPublicKey("test-public-key");
		caService.save(newCa);

		assertEquals(initialCaSizeInDB + 1, testHelper.findAllCaEntity().size());
	}

	@Test
	void getEligibleForSigning() {
		int numberOfCasWithPrivateKey = initialCaSizeInDB;
		assertEquals(numberOfCasWithPrivateKey, caService.getEligibleForSigning().size());
	}

	@SneakyThrows
	@Test
	void importCa() {
		// Arrange
		CaEntity importedCaEntity = new CaEntity();
		importedCaEntity.setCertificate("Test Certificate");
		importedCaEntity.setCn("Test Cn");
		importedCaEntity.setIssued(Instant.now());
		importedCaEntity.setName("Test Name");
		importedCaEntity.setPrivateKey("Test Private Key");
		importedCaEntity.setPublicKey("Test Public Key");

		// Load files from the test resources folder
		Path publicKeyPath =
			Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("public-key-test.txt")).toURI());
		Path privateKeyPath =
			Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("private-key-test.txt")).toURI());
		Path certificatePath =
			Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("certificate-test.txt")).toURI());

		// Create FormValue mocks for each file
		FormValue publicKeyFormValue = mock(FormValue.class);
		when(publicKeyFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(publicKeyPath));

		FormValue privateKeyFormValue = mock(FormValue.class);
		when(privateKeyFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(privateKeyPath));

		FormValue certificateFormValue = mock(FormValue.class);
		when(certificateFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(certificatePath));

		// Create DefaultFileUpload instances
		DefaultFileUpload publicKey = new DefaultFileUpload("Test Public Key", publicKeyFormValue);
		DefaultFileUpload privateKey = new DefaultFileUpload("Test Private Key", privateKeyFormValue);
		DefaultFileUpload certificate = new DefaultFileUpload("Test Certificate", certificateFormValue);


		// Act
		CaEntity caEntity = caService.importCa(importedCaEntity, publicKey, privateKey, certificate);

		// Assert
		assertNotNull(caEntity);
		assertEquals("Test Name", caEntity.getName());
		assertNotNull(caEntity.getPublicKey());
		assertNotNull(caEntity.getPrivateKey());
		assertNotNull(caEntity.getCertificate());
	}

	@Test
	void getPrivateKey() {
		// Arrange
		String validCaId = testHelper.findOneCaEntity().getId().toString();

		// Act
		String privateKey = caService.getPrivateKey(validCaId);

		// Assert
		assertEquals("test-private-key", privateKey);
	}

	@Test
	void getPublicKey() {
		// Arrange
		String validCaId = testHelper.findOneCaEntity().getId().toString();

		// Act
		String publicKey = caService.getPublicKey(validCaId);

		// Assert
		assertEquals("test-public-key", publicKey);
	}

	@Test
	void getCertificate() {
		// Arrange
		CaEntity ca = testHelper.findOneCaEntityWithParentCa();
		String caId = ca.getId().toString();

		// Act
		List<String> certificate = caService.getCertificate(caId);

		// Assert
		assertEquals(2, certificate.size());
		assertEquals("test-cert-pem-format", certificate.getFirst());
	}

	@Test
	void deleteById() {
		// Arrange
		String validCaId = testHelper.findOneCaEntity().getId().toString();

		// Act
		caService.deleteById(validCaId);

		// Assert
		assertEquals(initialCaSizeInDB - 1, testHelper.findAllCaEntity().size());
	}

	@Test
	void findFirstByColumn() {
		// Arrange
		CaEntity ca = testHelper.findOneCaEntityWithParentCa();
		Map<String, Object> fields = Map.of(
			"cn", ca.getCn(),
			"issued", ca.getIssued(),
			"validity", ca.getValidity(),
			"publicKey", ca.getPublicKey(),
			"privateKey", ca.getPrivateKey(),
			"certificate", ca.getCertificate(),
			"parentCa", ca.getParentCa(),
			"parentCaId", ca.getParentCaId(),
			"name", ca.getName()
		);

		// Act & Assert
		for (Map.Entry<String, Object> entry : fields.entrySet()) {
			String column = entry.getKey();
			Object value = entry.getValue();

			CaEntity nonexistentEntity = caService.findFirstByColumn(column, "nonexistent");
			CaEntity existentEntity = caService.findFirstByColumn(column, value);

			assertNull(nonexistentEntity, "Expected null for column: " + column + " with value: nonexistent");
			assertNotNull(existentEntity, "Expected entity for column: " + column + " with value: " + value + " but found null");
		}

		// Special case: Testing nonexistent column
		CaEntity caNonexistent = caService.findFirstByColumn("nonexistent", "test");
		assertNull(caNonexistent, "Expected null for nonexistent column");
	}

	@Test
	void findById() {
		// Arrange
		String validCaId = testHelper.findOneCaEntity().getId().toString();

		// Act & Assert
		CaEntity caEntity = caService.findById(validCaId);
		assertNotNull(caEntity);
	}

	@Test
	void find() {
		// Act
		List<CaEntity> caEntities = caService.find(testHelper.makePageable(0, 100), true).getContent();

		// Assert
		assertEquals(initialCaSizeInDB, caEntities.size());
	}
}
