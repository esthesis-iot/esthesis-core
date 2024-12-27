package esthesis.service.crypto.impl;

import esthesis.core.common.AppConstants;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.dto.KeystoreEntryDTO;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.entity.KeystoreEntity;
import esthesis.service.crypto.impl.repository.CaEntityRepository;
import esthesis.service.crypto.impl.repository.CertificateEntityRepository;
import esthesis.service.crypto.impl.repository.KeystoreEntityRepository;
import esthesis.service.device.dto.DeviceKeyDTO;
import esthesis.service.device.entity.DeviceEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.mockito.Mockito;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static esthesis.common.util.EsthesisCommonConstants.Device.Type.CORE;
import static esthesis.core.common.AppConstants.Device.Status.REGISTERED;
import static esthesis.core.common.AppConstants.Keystore.Item.*;
import static esthesis.core.common.AppConstants.Keystore.Item.ResourceType.CA;
import static esthesis.core.common.AppConstants.Keystore.Item.ResourceType.CERT;
import static esthesis.core.common.AppConstants.Keystore.Item.ResourceType.DEVICE;
import static esthesis.core.common.AppConstants.Keystore.Item.ResourceType.TAG;
import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

@ApplicationScoped
public class TestHelper {

	@Inject
	CaEntityRepository caEntityRepository;

	@Inject
	CertificateEntityRepository certificateEntityRepository;

	@Inject
	KeystoreEntityRepository keystoreEntityRepository;

	public CaEntity makeCaEntity(CaEntity parentCa) {
		CaEntity ca = new CaEntity();
		ca.setCn("test-cn");
		ca.setIssued(Instant.now());
		ca.setValidity(Instant.now().plus(360, ChronoUnit.DAYS));
		ca.setName("Test CA");
		ca.setPrivateKey(getValidPrivateKey());
		ca.setPublicKey(getValidPublicKey());
		ca.setCertificate(getValidCertificate());

		if (parentCa != null) {
			ca.setParentCaId(parentCa.getId());
			ca.setParentCa(parentCa.getCn());
		}

		return ca;
	}

	public CertificateEntity makeCertificateEntity(CaEntity caEntity) {
		CertificateEntity certificate = new CertificateEntity();
		certificate.setCn("test-cert-cn");
		certificate.setIssued(Instant.now());
		certificate.setValidity(Instant.now().plus(360, ChronoUnit.DAYS));
		certificate.setIssuer(caEntity.getCn());
		certificate.setName("Test Cert");
		certificate.setKeyAlgorithm("test-key-algorithm");
		certificate.setCertificate(getValidCertificate());
		certificate.setPrivateKey(getValidPrivateKey());
		certificate.setPublicKey(getValidPublicKey());
		certificate.setSignatureAlgorithm("test-signature-algorithm");
		certificate.setSan("test-san");

		return certificate;
	}

	public KeystoreEntity makeKeystoreEntity(String certificateId, String caId) {
		return Instancio.of(KeystoreEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.set(field(KeystoreEntity.class, "type"), "PKCS12/SunJSSE")
			.set(field(KeystoreEntity.class, "entries"), List.of(
					makeKeyStoreEntry("test-entry-id-1", DEVICE, List.of(KeyType.PRIVATE, KeyType.CERT)),
					makeKeyStoreEntry(certificateId, CERT, List.of(KeyType.PRIVATE, KeyType.CERT)),
					makeKeyStoreEntry(caId, CA, List.of(KeyType.PRIVATE, KeyType.CERT)),
					makeKeyStoreEntry("test-entry-id-4", TAG, List.of(KeyType.PRIVATE, KeyType.CERT))
				)
			)
			.create();
	}

	/**
	 * Generates a Certificate Authority (CA) entity, a Certificate entity using the created CA,
	 * and a Keystore entity. The entities are then persisted in their respective repositories.
	 */
	public void createEntities(CaEntity pareCaEntity) {
		CaEntity caEntity = makeCaEntity(pareCaEntity);
		caEntityRepository.persist(caEntity);
		CertificateEntity certificateEntity = makeCertificateEntity(caEntity);
		certificateEntityRepository.persist(certificateEntity);
		KeystoreEntity keystoreEntity =
			makeKeystoreEntity(certificateEntity.getId().toString(), caEntity.getId().toString());
		keystoreEntityRepository.persist(keystoreEntity);
	}

	public void clearDatabase() {
		caEntityRepository.deleteAll();
		certificateEntityRepository.deleteAll();
		keystoreEntityRepository.deleteAll();
	}

	public CaEntity findOneCaEntity() {
		return caEntityRepository.findAll().firstResult();
	}

	public CaEntity findOneCaEntityWithParentCa() {
		return caEntityRepository
			.findAll()
			.stream()
			.filter(ca -> ca.getParentCa() != null)
			.findFirst()
			.orElse(null);
	}

	public List<CaEntity> findAllCaEntity() {
		return caEntityRepository.findAll().list();
	}

	public List<CertificateEntity> findAllCertificateEntity() {
		return certificateEntityRepository.findAll().list();
	}

	public List<KeystoreEntity> findAllKeystoreEntity() {
		return keystoreEntityRepository.findAll().list();
	}

	/**
	 * Helper method to create a Pageable object with the specified parameters
	 */
	public Pageable makePageable(int page, int size) {

		// Create a mock of UriInfo
		UriInfo uriInfo = Mockito.mock(UriInfo.class);

		// Define the behavior of the mock
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/find?page=" + page + "&size=" + size));
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

		Pageable pageable = new Pageable();
		pageable.setPage(page);
		pageable.setSize(size);
		pageable.setSort("");
		pageable.setUriInfo(uriInfo);
		return pageable;
	}

	public CertificateEntity findOneCertificateEntity() {
		return certificateEntityRepository.findAll().firstResult();
	}

	@SneakyThrows
	public DeviceEntity makeDeviceEntity(String hardwareId) {


		// Create a device key with valid certificate, private key, and public key
		DeviceKeyDTO deviceKey = new DeviceKeyDTO();
		deviceKey.setCertificate(getValidCertificate());
		deviceKey.setPrivateKey(getValidPrivateKey());
		deviceKey.setPublicKey(getValidPublicKey());

		return new DeviceEntity()
			.setHardwareId(hardwareId)
			.setType(CORE)
			.setCreatedOn(Instant.now().minus(1, ChronoUnit.DAYS))
			.setTags(List.of("test"))
			.setRegisteredOn(Instant.now().minus(1, ChronoUnit.DAYS))
			.setLastSeen(Instant.now().minus(1, ChronoUnit.MINUTES))
			.setDeviceKey(deviceKey)
			.setStatus(REGISTERED);
	}

	public KeystoreEntity findOneKeystoreEntity() {
		return keystoreEntityRepository.findAll().firstResult();
	}

	public KeystoreEntity findOneKeystoreEntityById(String id) {
		return keystoreEntityRepository
			.findAll()
			.stream()
			.filter(keystore -> keystore.getId().toString().equals(id))
			.findFirst()
			.orElse(null);
	}

	private KeystoreEntryDTO makeKeyStoreEntry(String id,
																						 AppConstants.Keystore.Item.ResourceType resourceType,
																						 List<AppConstants.Keystore.Item.KeyType> keyType) {
		return Instancio.of(KeystoreEntryDTO.class)
			.set(field(KeystoreEntryDTO.class, "id"), id)
			.set(field(KeystoreEntryDTO.class, "resourceType"), resourceType)
			.set(field(KeystoreEntryDTO.class, "keyType"), keyType)
			.create();
	}

	@SneakyThrows
	private String getValidPublicKey() {
		Path publicKeyPath =
			Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("public-key-test.txt")).toURI());
		return Files.readString(publicKeyPath);
	}

	@SneakyThrows
	private String getValidPrivateKey() {
		Path privateKeyPath =
			Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("private-key-test.txt")).toURI());
		return Files.readString(privateKeyPath);
	}

	@SneakyThrows
	private String getValidCertificate() {
		Path certificatePath =
			Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("certificate-test.txt")).toURI());
		return Files.readString(certificatePath);
	}
}
