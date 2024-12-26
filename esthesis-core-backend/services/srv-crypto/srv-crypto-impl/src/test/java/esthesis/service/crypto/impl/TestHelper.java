package esthesis.service.crypto.impl;

import esthesis.core.common.entity.BaseEntity;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.entity.KeystoreEntity;
import esthesis.service.crypto.impl.repository.CaEntityRepository;
import esthesis.service.crypto.impl.repository.CertificateEntityRepository;
import esthesis.service.crypto.impl.repository.KeystoreEntityRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.instancio.Instancio;
import org.mockito.Mockito;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
		ca.setPrivateKey("test-private-key");
		ca.setPublicKey("test-public-key");
		ca.setCertificate("test-cert-pem-format");

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
		certificate.setCertificate("test-cert-pem-format");
		certificate.setName("Test Cert");
		certificate.setKeyAlgorithm("test-key-algorithm");
		certificate.setPrivateKey("test-private-key");
		certificate.setPublicKey("test-public-key");
		certificate.setSignatureAlgorithm("test-signature-algorithm");
		certificate.setSan("test-san");

		return certificate;
	}

	public KeystoreEntity makeKeystoreEntity() {
		return Instancio.of(KeystoreEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
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
		KeystoreEntity keystoreEntity = makeKeystoreEntity();
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
}
