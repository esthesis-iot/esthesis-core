package esthesis.service.crypto.impl;

import esthesis.core.common.entity.BaseEntity;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.entity.KeystoreEntity;
import esthesis.service.crypto.impl.repository.CaEntityRepository;
import esthesis.service.crypto.impl.repository.CertificateEntityRepository;
import esthesis.service.crypto.impl.repository.KeystoreEntityRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.instancio.Instancio;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.instancio.Select.all;
import static org.instancio.Select.field;

@ApplicationScoped
public class TestHelper {

	@Inject
	CaEntityRepository caEntityRepository;

	@Inject
	CertificateEntityRepository certificateEntityRepository;

	@Inject
	KeystoreEntityRepository keystoreEntityRepository;

	public CaEntity makeCaEntity(CertificateEntity parentCa) {
		CaEntity ca = new CaEntity();
		ca.setCn("test-ca");
		ca.setIssued(Instant.now());
		ca.setValidity(Instant.now().plus(360, ChronoUnit.DAYS));
		ca.setName("Test CA");
		ca.setPrivateKey("test-private-key");
		ca.setPublicKey("test-public-key");

		if (parentCa != null) {
			ca.setParentCaId(parentCa.getId());
			ca.setParentCa(parentCa.getCn());
		}

		return ca;
	}

	public CertificateEntity makeCertificateEntity(CaEntity caEntity) {
		CertificateEntity certificate = new CertificateEntity();
		certificate.setCn("test-cert");
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
	public void createEntities() {
		CaEntity caEntity = makeCaEntity(null);
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

	public CaEntity findOneCaEntity(){
		return caEntityRepository.findAll().firstResult();
	}

	public List<CaEntity> findAllCaEntity(){
		return caEntityRepository.findAll().list();
	}

	public List<CertificateEntity> findAllCertificateEntity() {
		return certificateEntityRepository.findAll().list();
	}

	public List<KeystoreEntity> findAllKeystoreEntity() {
		return keystoreEntityRepository.findAll().list();
	}
}
