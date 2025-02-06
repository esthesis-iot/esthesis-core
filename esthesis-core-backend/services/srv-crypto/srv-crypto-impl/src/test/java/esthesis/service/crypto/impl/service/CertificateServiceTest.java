package esthesis.service.crypto.impl.service;

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

import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.impl.TestHelper;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.server.core.multipart.DefaultFileUpload;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

	@Inject
	CAService caService;

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

	@SneakyThrows
	@Test
	void importCertificate() {

		// Prepare a certificate to be imported.

		CertificateEntity certificate = new CertificateEntity();
		certificate.setCertificate("Test Certificate");
		certificate.setCn("Test Cn");
		certificate.setIssued(Instant.now());
		certificate.setName("Test Name");
		certificate.setPrivateKey("Test Private Key");
		certificate.setPublicKey("Test Public Key");

		// Prepare mocks for file upload.
		Path publicKeyPath =
			Paths.get(
				Objects.requireNonNull(getClass().getClassLoader().getResource("public-key-test.txt"))
					.toURI());
		Path privateKeyPath =
			Paths.get(
				Objects.requireNonNull(getClass().getClassLoader().getResource("private-key-test.txt"))
					.toURI());
		Path certificatePath =
			Paths.get(
				Objects.requireNonNull(getClass().getClassLoader().getResource("certificate-test.txt"))
					.toURI());

		// Create FormValue mocks for each file.
		FormValue publicKeyFormValue = mock(FormValue.class);
		when(publicKeyFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(publicKeyPath));

		FormValue privateKeyFormValue = mock(FormValue.class);
		when(privateKeyFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(privateKeyPath));

		FormValue certificateFormValue = mock(FormValue.class);
		when(certificateFormValue.getFileItem()).thenReturn(new FormData.FileItemImpl(certificatePath));

		// Create DefaultFileUpload instances.
		DefaultFileUpload publicKey = new DefaultFileUpload("Test Public Key", publicKeyFormValue);
		DefaultFileUpload privateKey = new DefaultFileUpload("Test Private Key", privateKeyFormValue);
		DefaultFileUpload certificateFileUpload = new DefaultFileUpload("Test Certificate",
			certificateFormValue);

		// Perform the import operation.
		CertificateEntity certificateImported =
			certificateService.importCertificate(certificate, publicKey, privateKey,
				certificateFileUpload);

		// Assert certificate was imported.
		assertNotNull(certificateImported);
		assertEquals("Test Name", certificateImported.getName());
		assertNotNull(certificateImported.getPublicKey());
		assertNotNull(certificateImported.getPrivateKey());
		assertNotNull(certificateImported.getCertificate());

	}

	@Test
	void save() {
		// Arrange a new certificate.
		CertificateEntity newCertificate = new CertificateEntity();
		newCertificate.setCn("test-new-certificate");
		newCertificate.setIssued(Instant.now());
		newCertificate.setValidity(Instant.now().plus(360, ChronoUnit.DAYS));
		newCertificate.setName("Test new certificate");
		newCertificate.setPrivateKey("test-private-key");
		newCertificate.setPublicKey("test-public-key");
		newCertificate.setSignatureAlgorithm("test-signature-algorithm");
		newCertificate.setSan("test-san");

		// Perform the save operation for the new certificate.
		String certificateId = certificateService.save(newCertificate).getId().toHexString();

		// Assert certificate was saved with the provided values.
		CertificateEntity savedCertificate = certificateService.findById(certificateId);
		assertEquals("test-new-certificate", savedCertificate.getCn());
		assertNotNull(savedCertificate.getIssued());
		assertNotNull(savedCertificate.getValidity());
		assertNotNull(savedCertificate.getPrivateKey());
		assertEquals("Test new certificate", savedCertificate.getName());
		assertEquals("test-san", savedCertificate.getSan());
	}

	@Test
	void getPrivateKey() {
		// Perform a save operation for a new CA.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		//  Perform a save operation for a new certificate.
		String certificateId = certificateService.save(testHelper.makeCertificateEntity(ca)).getId()
			.toHexString();

		// Assert private key can be retrieved.
		assertNotNull(certificateService.getPrivateKey(certificateId));
	}

	@Test
	void getPublicKey() {
		// Perform a save operation for a new CA.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		//  Perform a save operation for a new certificate.
		String certificateId = certificateService.save(testHelper.makeCertificateEntity(ca)).getId()
			.toHexString();

		// Assert public key can be retrieved.
		assertNotNull(certificateService.getPublicKey(certificateId));
	}

	@Test
	void getCertificate() {
		// Perform a save operation for a new CA.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		//  Perform a save operation for a new certificate.
		String certificateId = certificateService.save(testHelper.makeCertificateEntity(ca)).getId()
			.toHexString();

		// Assert certificate can be retrieved.
		assertNotNull(certificateService.getCertificate(certificateId));
	}

	@Test
	void deleteById() {
		// Perform a save operation for a new CA.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		//  Perform a save operation for a new certificate.
		String certificateId = certificateService.save(testHelper.makeCertificateEntity(ca)).getId()
			.toHexString();

		// Assert certificate exists.
		assertNotNull(certificateService.findById(certificateId));

		// Perform the delete operation for the certificate.
		certificateService.deleteById(certificateId);

		// Assert certificate was deleted.
		assertNull(certificateService.findById(certificateId));

	}

	@Test
	void findById() {
		// Perform a save operation for a new CA.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		//  Perform a save operation for a new certificate.
		String certificateId = certificateService.save(testHelper.makeCertificateEntity(ca)).getId()
			.toHexString();

		// Assert certificate can be found by id.
		assertNotNull(certificateService.findById(certificateId));
	}

	@Test
	void find() {
		// Assert no certificates are found.
		assertTrue(certificateService.find(testHelper.makePageable(0, 100)).getContent().isEmpty());

		// Perform a save operation for a new CA.
		CaEntity ca = caService.save(testHelper.makeCaEntity(null));

		//  Perform a save operation for a new certificate.
		certificateService.save(testHelper.makeCertificateEntity(ca));

		// Assert certificate can be found.
		assertFalse(certificateService.find(testHelper.makePageable(0, 100)).getContent().isEmpty());
	}
}
