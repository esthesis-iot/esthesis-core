package esthesis.service.crypto.impl.service;

import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.CRYPTO;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;

import esthesis.common.exception.QCouldNotSaveException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QMutationNotPermittedException;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.common.validation.CVEBuilder;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.impl.dto.CAHolderDTO;
import esthesis.service.crypto.impl.dto.CreateCARequestDTO;
import esthesis.service.crypto.impl.dto.CreateKeyPairRequestDTO;
import esthesis.service.crypto.impl.util.SrvCryptoCryptoConverters;
import esthesis.service.crypto.impl.util.SrvCryptoCryptoUtil;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import esthesis.util.kafka.notifications.outgoing.KafkaNotification;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Certificate Authority management.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class CAService extends BaseService<CaEntity> {

	@Inject
	@RestClient
	SettingsResource settingsResource;

	private List<String> getCertificate(String caId, List<String> certificteChain) {
		CaEntity caEntity = findById(caId);
		certificteChain.add(caEntity.getCertificate());

		if (caEntity.getParentCaId() != null) {
			getCertificate(caEntity.getParentCaId().toHexString(), certificteChain);
		}

		return certificteChain;
	}

	@Override
	@KafkaNotification(component = Component.CA, subject = Subject.CA,
		action = Action.CREATEORUPDATE, idParamRegEx = "BaseEntity\\(id=(.*?)\\)")
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = CREATE)
	public CaEntity save(CaEntity caEntity) {
		// CAs can not be edited, so throw an exception in that case.
		if (caEntity.getId() != null) {
			throw new QMutationNotPermittedException("A CA can not be edited once created.");
		}

		// Check a CA with the same name doesn't already exist.
		if (findFirstByColumn("cn", caEntity.getCn()) != null) {
			CVEBuilder.addAndThrow("cn", "A Certificate Authority with CN {} already exists.",
				caEntity.getCn());
		}

		// Create the CA.
		try {
			String asymmetricKeyAlgorithm =
				settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString();
			CreateCARequestDTO.CreateCARequestDTOBuilder createCARequestBuilder = CreateCARequestDTO.builder()
				.createKeyPairRequestDTO(CreateKeyPairRequestDTO.builder().keySize(
						settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE).asInt())
					.keyPairGeneratorAlgorithm(asymmetricKeyAlgorithm).build())
				.subjectCN(caEntity.getCn()).locale(Locale.US)
				.serial(BigInteger.valueOf(1)).signatureAlgorithm(
					settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM)
						.asString()).validFrom(Instant.now()).validTo(caEntity.getValidity());

			// If this CA has a parent CA (i.e. it is a sub-CA) fetch the details of the parent.
			if (caEntity.getParentCaId() != null) {
				CaEntity parentCaEntity = findById(caEntity.getParentCaId().toHexString());
				createCARequestBuilder.issuerCN(parentCaEntity.getCn())
					.issuerPrivateKeyAlgorithm(asymmetricKeyAlgorithm)
					.issuerPrivateKey(
						SrvCryptoCryptoConverters.pemToPrivateKey(parentCaEntity.getPrivateKey(),
							asymmetricKeyAlgorithm));
				caEntity.setParentCa(parentCaEntity.getCn());
			}

			final CAHolderDTO caHolderDTO = SrvCryptoCryptoUtil.createCA(createCARequestBuilder.build());
			caEntity.setCertificate(
				SrvCryptoCryptoConverters.certificateToPEM(caHolderDTO.getCertificate()));
			caEntity.setPrivateKey(
				SrvCryptoCryptoConverters.privateKeyToPEM(caHolderDTO.getPrivateKey()));
			caEntity.setPublicKey(SrvCryptoCryptoConverters.publicKeyToPEM(caHolderDTO.getPublicKey()));
			caEntity.setIssued(Instant.now());

			return super.save(caEntity);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException |
						 OperatorCreationException | NoSuchProviderException e) {
			throw new QCouldNotSaveException("Could not save CA.", e);
		}
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public List<CaEntity> getEligibleForSigning() {
		return getRepository().find("privateKey != null", Sort.ascending("cn")).list();
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = CREATE)
	public CaEntity importCa(CaEntity importedCaEntity, FileUpload publicKey, FileUpload privateKey,
		FileUpload certificate) {
		CaEntity caEntity = new CaEntity();
		try {
			// Set the keys into the certificate entity.
			caEntity.setPublicKey(
				Files.readString(publicKey.uploadedFile().toAbsolutePath()));
			caEntity.setPrivateKey(
				Files.readString(privateKey.uploadedFile().toAbsolutePath()));
			caEntity.setCertificate(
				Files.readString(certificate.uploadedFile().toAbsolutePath()));

			// Extract additional certificate information.
			X509Certificate x509Certificate = SrvCryptoCryptoConverters.pemToCertificate(
				caEntity.getCertificate());
			caEntity.setName(importedCaEntity.getName());
			caEntity.setIssued(x509Certificate.getNotBefore().toInstant());
			caEntity.setValidity(x509Certificate.getNotAfter().toInstant());

			//Set CN and parent CA.
			String cn = SrvCryptoCryptoUtil.cleanUpCn(
				x509Certificate.getSubjectX500Principal().getName());
			String issuer = SrvCryptoCryptoUtil.cleanUpCn(
				x509Certificate.getIssuerX500Principal().getName());
			caEntity.setCn(cn);
			if (!cn.equalsIgnoreCase(issuer)) {
				caEntity.setParentCa(issuer);
			}

			super.getRepository().persistOrUpdate(caEntity);
			return caEntity;
		} catch (IOException | CertificateException e) {
			throw new QMismatchException("Could not import certificate.", e);
		}
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public String getPrivateKey(String caId) {
		CaEntity caEntity = findById(caId);

		return caEntity.getPrivateKey();
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public String getPublicKey(String caId) {
		CaEntity caEntity = findById(caId);

		return caEntity.getPublicKey();
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public List<String> getCertificate(String caId) {
		List<String> certificateChain = new ArrayList<>();
		getCertificate(caId, certificateChain);

		return certificateChain;
	}

	@Override
	@KafkaNotification(component = Component.CA, subject = Subject.CA,
		action = Action.DELETE, idParamOrder = 0)
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = DELETE)
	public boolean deleteById(String caId) {
		return super.deleteById(caId);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public CaEntity findFirstByColumn(String column, Object value) {
		return super.findFirstByColumn(column, value);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public CaEntity findById(String id) {
		return super.findById(id);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public Page<CaEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}
}
