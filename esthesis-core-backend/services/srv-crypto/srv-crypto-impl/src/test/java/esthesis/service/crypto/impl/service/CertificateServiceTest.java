package esthesis.service.crypto.impl.service;

import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.impl.TestHelper;
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
import java.util.Objects;

import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class CertificateServiceTest {

	@Inject
	TestHelper testHelper;

	@Inject
	CertificateService certificateService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsResource settingsResource;


	int initialCertificateSizeInDB = 0;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

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

	@SneakyThrows
	@Test
	void importCertificate() {

		// Arrange - Create a certificate entity

		CertificateEntity certificate = new CertificateEntity();
		certificate.setCertificate("Test Certificate");
		certificate.setCn("Test Cn");
		certificate.setIssued(Instant.now());
		certificate.setName("Test Name");
		certificate.setPrivateKey("Test Private Key");
		certificate.setPublicKey("Test Public Key");

		// Arrange - Load files from the test resources folder
		Path publicKeyPath =
			Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("public-key-test.txt")).toURI());
		Path privateKeyPath =
			Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("private-key-test.txt")).toURI());
		Path certificatePath =
			Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("certificate-test.txt")).toURI());

		// Arrange - Create FormValue mocks for each file
		FormValue publicKeyFormValue = mock(FormValue.class);
		when(publicKeyFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(publicKeyPath));

		FormValue privateKeyFormValue = mock(FormValue.class);
		when(privateKeyFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(privateKeyPath));

		FormValue certificateFormValue = mock(FormValue.class);
		when(certificateFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(certificatePath));

		// Arrange - Create DefaultFileUpload instances
		DefaultFileUpload publicKey = new DefaultFileUpload("Test Public Key", publicKeyFormValue);
		DefaultFileUpload privateKey = new DefaultFileUpload("Test Private Key", privateKeyFormValue);
		DefaultFileUpload certificateFileUpload = new DefaultFileUpload("Test Certificate", certificateFormValue);

		// Act
		CertificateEntity certificateImported =
			certificateService.importCertificate(certificate, publicKey, privateKey, certificateFileUpload);

		// Assert
		assertNotNull(certificateImported);
		assertEquals("Test Name", certificateImported.getName());
		assertNotNull(certificateImported.getPublicKey());
		assertNotNull(certificateImported.getPrivateKey());
		assertNotNull(certificateImported.getCertificate());

	}

	@Test
	void save() {
		// Arrange
		CertificateEntity newCertificate = new CertificateEntity();
		newCertificate.setCn("test-new-certificate");
		newCertificate.setIssued(Instant.now());
		newCertificate.setValidity(Instant.now().plus(360, ChronoUnit.DAYS));
		newCertificate.setName("Test new certificate");
		newCertificate.setPrivateKey("test-private-key");
		newCertificate.setPublicKey("test-public-key");
		newCertificate.setSignatureAlgorithm("test-signature-algorithm");
		newCertificate.setSan("test-san");

		// Act
		certificateService.save(newCertificate);

		// Assert
		assertEquals(initialCertificateSizeInDB + 1, testHelper.findAllCertificateEntity().size());
	}

	@Test
	void getPrivateKey() {
		// Arrange
		String validCertificateId = testHelper.findOneCertificateEntity().getId().toString();
		// Act
		String privateKey = certificateService.getPrivateKey(validCertificateId);
		// Assert
		assertNotNull(privateKey);
	}

	@Test
	void getPublicKey() {
		// Arrange
		String validCertificateId = testHelper.findOneCertificateEntity().getId().toString();
		// Act
		String publicKey = certificateService.getPublicKey(validCertificateId);
		// Assert
		assertNotNull(publicKey);
	}

	@Test
	void getCertificate() {
		// Arrange
		String validCertificateId = testHelper.findOneCertificateEntity().getId().toString();
		// Act
		String certificate = certificateService.getCertificate(validCertificateId);
		// Assert
		assertNotNull(certificate);

	}

	@Test
	void deleteById() {
		// Arrange
		String unexistentCertificateId = new ObjectId().toString();
		String validCertificateId = testHelper.findOneCertificateEntity().getId().toString();

		// Act try to delete an unexistent certificate
		certificateService.deleteById(unexistentCertificateId);

		// Assert nothing happens
		assertEquals(initialCertificateSizeInDB, testHelper.findAllCertificateEntity().size());

		// Act try to delete a valid certificate
		certificateService.deleteById(validCertificateId);

		// Assert the certificate is deleted
		assertEquals(initialCertificateSizeInDB - 1, testHelper.findAllCertificateEntity().size());

	}

	@Test
	void findById() {
		// Arrange
		String validCertificateId = testHelper.findOneCertificateEntity().getId().toString();

		// Act & Assert
		CertificateEntity certificateEntity = certificateService.findById(validCertificateId);
		assertNotNull(certificateEntity);
	}

	@Test
	void find() {
		// Act
		List<CertificateEntity> certificates =
			certificateService.find(testHelper.makePageable(0, 100), true).getContent();

		// Assert
		assertEquals(initialCertificateSizeInDB, certificates.size());
	}
}
