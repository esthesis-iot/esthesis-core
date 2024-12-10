package esthesis.service.crypto.impl.service;

import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.core.multipart.DefaultFileUpload;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class CAServiceTest {

	@Inject
	TestHelper testHelper;

	@Inject
	CAService caService;

	int initialCaSizeInDB = 0;
	int inititalCertificateSizeInDB = 0;
	int initialKeystoreSizeInDB = 0;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		testHelper.createEntities();

		initialCaSizeInDB = testHelper.findAllCaEntity().size();
		inititalCertificateSizeInDB = testHelper.findAllCertificateEntity().size();
		initialKeystoreSizeInDB = testHelper.findAllKeystoreEntity().size();

		log.info("Initial CA size in DB: {}", initialCaSizeInDB);
		log.info("Initial certificate size in DB: {}", inititalCertificateSizeInDB);
		log.info("Initial keystore size in DB: {}", initialKeystoreSizeInDB);

	}

	@Test
	void save() {

		//ToDO mock settingsResouce.findByName

		CaEntity newCa = new CaEntity();
		newCa.setCn("test-new-ca");
		newCa.setIssued(Instant.now());
		newCa.setValidity(Instant.now().plus(360, ChronoUnit.DAYS));
		newCa.setName("Test new CA");
		newCa.setPrivateKey("test-private-key");
		newCa.setPublicKey("test-public-key");
		caService.save(newCa);

		assertEquals(2, testHelper.findAllCaEntity().size());
	}

	@Test
	void getEligibleForSigning() {
		int numberOfCasWithPrivateKey = initialCaSizeInDB;
		assertEquals(numberOfCasWithPrivateKey, caService.getEligibleForSigning().size());
	}

	@Test
	void importCa() {
//		// Arrange
//		CaEntity importedCaEntity = new CaEntity();
//		importedCaEntity.setCertificate("Test Certificate");
//		importedCaEntity.setCn("Test Cn");
//		importedCaEntity.setIssued(Instant.now());
//		importedCaEntity.setName("Test Name");
//		importedCaEntity.setPrivateKey("Test Private Key");
//		importedCaEntity.setPublicKey("Test Public Key");
//
//		FormValue fileUploadMock = mock(FormValue.class);
//		when(fileUploadMock.getFileItem())
//			.thenReturn(new FormData.FileItemImpl(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
//
//		DefaultFileUpload publicKey = new DefaultFileUpload("Test Public Key", fileUploadMock);
//
//		DefaultFileUpload privateKey = new DefaultFileUpload("Test Private Key", fileUploadMock);
//
//		DefaultFileUpload certificate = new DefaultFileUpload("Test Certificate", fileUploadMock);
//
//
//		// Act
//		CaEntity caEntity = caService.importCa(importedCaEntity, publicKey, privateKey, certificate);
//
//		// Assert
//		assertNotNull(caEntity);
//		assertEquals("Test CA", caEntity.getName());
//		assertNotNull(caEntity.getPublicKey());
//		assertNotNull(caEntity.getPrivateKey());
//		assertNotNull(caEntity.getCertificate());
	}

	@Test
	void getPrivateKey() {
	}

	@Test
	void getPublicKey() {
	}

	@Test
	void getCertificate() {
	}

	@Test
	void deleteById() {
	}

	@Test
	void findFirstByColumn() {
	}

	@Test
	void findById() {
	}

	@Test
	void find() {
	}
}
