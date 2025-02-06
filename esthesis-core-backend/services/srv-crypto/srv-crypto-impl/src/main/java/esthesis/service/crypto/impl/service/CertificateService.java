package esthesis.service.crypto.impl.service;

import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.CRYPTO;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;

import esthesis.common.crypto.CryptoConvertersUtil;
import esthesis.common.crypto.CryptoUtil;
import esthesis.common.crypto.dto.CertificateSignRequestDTO;
import esthesis.common.crypto.dto.CreateCertificateRequestDTO;
import esthesis.common.crypto.dto.CreateKeyPairRequestDTO;
import esthesis.common.exception.QCouldNotSaveException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QMutationNotPermittedException;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.impl.repository.CaEntityRepository;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import esthesis.util.kafka.notifications.outgoing.KafkaNotification;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Service for managing certificates.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class CertificateService extends BaseService<CertificateEntity> {

	@Inject
	CAService caService;

	@Inject
	@RestClient
	SettingsResource settingsResource;

	@Inject
	CaEntityRepository caEntityRepository;

	/**
	 * Import a certificate.
	 *
	 * @param importedCertificateEntity The certificate entity to import.
	 * @param publicKey                 The public key file.
	 * @param privateKey                The private key file.
	 * @param certificate               The certificate file.
	 * @return The imported certificate entity.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = CREATE)
	public CertificateEntity importCertificate(CertificateEntity importedCertificateEntity,
		FileUpload publicKey, FileUpload privateKey, FileUpload certificate) {
		CertificateEntity certificateEntity = new CertificateEntity();

		try {
			// Set the keys into the certificate entity.
			certificateEntity.setPublicKey(
				Files.readString(publicKey.uploadedFile().toAbsolutePath()));
			certificateEntity.setPrivateKey(
				Files.readString(privateKey.uploadedFile().toAbsolutePath()));
			certificateEntity.setCertificate(
				Files.readString(certificate.uploadedFile().toAbsolutePath()));

			// Extract additional certificate information.
			X509Certificate x509Certificate = CryptoConvertersUtil.pemToCertificate(
				certificateEntity.getCertificate());
			certificateEntity.setCn(
				CryptoUtil.cleanUpCn(x509Certificate.getSubjectX500Principal().getName()));
			certificateEntity.setIssued(x509Certificate.getNotBefore().toInstant());
			certificateEntity.setValidity(x509Certificate.getNotAfter().toInstant());
			certificateEntity.setIssuer(
				CryptoUtil.cleanUpCn(x509Certificate.getIssuerX500Principal().getName()));
			certificateEntity.setName(importedCertificateEntity.getName());
			if (x509Certificate.getSubjectAlternativeNames() != null) {
				certificateEntity.setSan(x509Certificate.getSubjectAlternativeNames().stream()
					.map(san -> san.get(1).toString())
					.collect(Collectors.joining(",")));
			}

			super.getRepository().persist(certificateEntity);

			return certificateEntity;
		} catch (IOException | CertificateException e) {
			throw new QMismatchException("Could not import certificate.", e);
		}
	}

	@Override
	@KafkaNotification(component = Component.CERTIFICATE, subject = Subject.CERTIFICATE,
		action = Action.CREATEORUPDATE, idParamRegEx = "BaseEntity\\(id=(.*?)\\)")
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = CREATE)
	public CertificateEntity save(CertificateEntity certificateEntity) {
		// Certificates can not be edited, so throw an exception in that case.
		if (certificateEntity.getId() != null) {
			throw new QMutationNotPermittedException(
				"A certificate can not be edited once created.");
		}

		try {
			// Get the issuer CA.
			CaEntity caEntity = null;
			if (StringUtils.isNotBlank(certificateEntity.getIssuer())) {
				caEntity = caService.findFirstByColumn("cn", certificateEntity.getIssuer());
			}

			// Generate a keypair.
			final KeyPair keyPair = CryptoUtil.createKeyPair(
				CreateKeyPairRequestDTO.builder()
					.keySize(settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE).asInt())
					.keyPairGeneratorAlgorithm(
						settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
					.build()
			);

			// Prepare the sign request.
			CertificateSignRequestDTO certificateSignRequestDTO = new CertificateSignRequestDTO();
			certificateSignRequestDTO
				.setLocale(Locale.US)
				.setPrivateKey(keyPair.getPrivate())
				.setPublicKey(keyPair.getPublic())
				.setSignatureAlgorithm(settingsResource.findByName(
					NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM).asString())
				.setSubjectCN(certificateEntity.getCn())
				.setValidForm(Instant.now())
				.setValidTo(certificateEntity.getValidity());

			if (StringUtils.isNotEmpty(certificateEntity.getSan())) {
				certificateSignRequestDTO.setSan(
					Arrays.stream(certificateEntity.getSan().split(",")).map(String::trim)
						.collect(Collectors.joining(","))
				);
			}

			if (caEntity != null) {
				certificateSignRequestDTO.setIssuerCN(caEntity.getCn());
				certificateSignRequestDTO.setIssuerPrivateKey(
					CryptoConvertersUtil.pemToPrivateKey(
						caEntity.getPrivateKey(),
						settingsResource.findByName(
								NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM)
							.asString()));
			} else {
				certificateSignRequestDTO.setIssuerCN(certificateEntity.getCn());
				certificateSignRequestDTO.setIssuerPrivateKey(keyPair.getPrivate());
			}

			// Sign the certificate.
			final X509CertificateHolder x509CertificateHolder =
				CryptoUtil.generateCertificate(certificateSignRequestDTO);

			// Generate the PEM version of the certificate.
			String certPEM = CryptoConvertersUtil.certificateToPEM(
				x509CertificateHolder.toASN1Structure());

			// Add the certificate chain.
			if (caEntity != null) {
				certPEM = certPEM + String.join("",
					caService.getCertificate(caEntity.getId().toHexString()));
			}

			// Populate the certificate DTO to persist it.
			certificateEntity.setIssued(certificateSignRequestDTO.getValidForm());
			certificateEntity
				.setPrivateKey(CryptoConvertersUtil.privateKeyToPEM(keyPair.getPrivate()));
			certificateEntity.setPublicKey(CryptoConvertersUtil.publicKeyToPEM(keyPair.getPublic()));
			certificateEntity.setIssuer(certificateSignRequestDTO.getIssuerCN());
			certificateEntity.setCertificate(certPEM);
			certificateEntity.setSan(certificateSignRequestDTO.getSan());
			certificateEntity.setName(certificateEntity.getName());
			certificateEntity.setKeyAlgorithm(
				settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());
			certificateEntity.setSignatureAlgorithm(
				settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM)
					.asString());

			return super.save(certificateEntity);
		} catch (NoSuchAlgorithmException | IOException |
						 OperatorCreationException |
						 InvalidKeySpecException | NoSuchProviderException e) {
			throw new QCouldNotSaveException("Could not save certificate.", e);
		}
	}

	/**
	 * Get the private key of a certificate.
	 *
	 * @param certId The certificate ID.
	 * @return The private key.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public String getPrivateKey(String certId) {
		CertificateEntity certificateEntity = findById(certId);

		return certificateEntity.getPrivateKey();
	}

	/**
	 * Get the public key of a certificate.
	 *
	 * @param certId The certificate ID.
	 * @return The public key.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public String getPublicKey(String certId) {
		CertificateEntity certificateEntity = findById(certId);

		return certificateEntity.getPublicKey();
	}

	/**
	 * Get the certificate of a certificate.
	 *
	 * @param certId The certificate ID.
	 * @return The certificate.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public String getCertificate(String certId) {
		CertificateEntity certificateEntity = findById(certId);

		return certificateEntity.getCertificate();
	}

	/**
	 * Delete a certificate by ID.
	 *
	 * @param id The ID of the entity to delete.
	 * @return True if the entity was deleted, false otherwise.
	 */
	@Override
	@KafkaNotification(component = Component.CERTIFICATE, subject = Subject.CERTIFICATE,
		action = Action.DELETE, idParamOrder = 0)
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = DELETE)
	public boolean deleteById(String id) {
		return super.deleteById(id);
	}

	/**
	 * Find a certificate by ID.
	 *
	 * @param id The ID of the entity to find.
	 * @return The entity.
	 */
	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public CertificateEntity findById(String id) {
		return super.findById(id);
	}

	/**
	 * Find all certificates.
	 *
	 * @param pageable Representation of page, size, and sort search parameters.
	 * @return The page of entities.
	 */
	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public Page<CertificateEntity> find(Pageable pageable) {
		return super.find(pageable);
	}

	/**
	 * Generate a certificate as PEM.
	 *
	 * @param createCertificateRequestDTO The request to create a certificate.
	 * @return The certificate as PEM.
	 * @throws NoSuchAlgorithmException  If the algorithm is not found.
	 * @throws InvalidKeySpecException   If the key spec is invalid.
	 * @throws OperatorCreationException If the operator can not be created.
	 * @throws IOException               If an I/O error occurs.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public String generateCertificateAsPEM(CreateCertificateRequestDTO createCertificateRequestDTO)
	throws NoSuchAlgorithmException, InvalidKeySpecException, OperatorCreationException, IOException {
		// Prepare a certificate sign request.
		// Create a public key.
		PublicKey publicKey = KeyFactory.getInstance(
				settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
			.generatePublic(new X509EncodedKeySpec(
				createCertificateRequestDTO.getKeyPair().getPublic().getEncoded()));

		// Create a private key.
		PrivateKey privateKey = KeyFactory.getInstance(
				settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
			.generatePrivate(new PKCS8EncodedKeySpec(
				createCertificateRequestDTO.getKeyPair().getPrivate().getEncoded()));

		// Create a certificate sign request.
		CertificateSignRequestDTO certificateSignRequestDTO =
			new CertificateSignRequestDTO()
				.setLocale(Locale.getDefault())
				.setPrivateKey(privateKey)
				.setPublicKey(publicKey)
				.setValidForm(Instant.now())
				.setSignatureAlgorithm(
					settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM)
						.asString()).setSubjectCN(createCertificateRequestDTO.getCn());

		// If a root CA is defined, use it to sign the certificate. Otherwise, self-sign the certificate.
		SettingEntity caRegistryEntry = settingsResource.findByName(NamedSetting.DEVICE_ROOT_CA);
		if (caRegistryEntry == null || StringUtils.isBlank(caRegistryEntry.getValue())) {
			Log.warn("No root CA is defined, will generate a self-signed certificate.");
			certificateSignRequestDTO
				.setValidTo(Instant.now().plus(3650, ChronoUnit.DAYS))
				.setIssuerCN(createCertificateRequestDTO.getCn())
				.setIssuerPrivateKey(privateKey);
		} else {
			final CaEntity caEntity = caEntityRepository.findById(caRegistryEntry.asObjectId());
			certificateSignRequestDTO
				.setValidTo(caEntity.getValidity())
				.setIssuerCN(caEntity.getCn())
				.setIssuerPrivateKey(
					CryptoConvertersUtil.pemToPrivateKey(caEntity.getPrivateKey(),
						settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM)
							.asString()));
		}

		// Sign the certificate.
		X509CertificateHolder x509CertificateHolder = CryptoUtil.generateCertificate(
			certificateSignRequestDTO);

		// Convert the certificate to PEM.
		String cert = CryptoConvertersUtil.certificateToPEM(
			x509CertificateHolder.toASN1Structure());

		// Add certificate chain, if requested.
		if (createCertificateRequestDTO.isIncludeCertificateChain() && !(caRegistryEntry == null
			|| StringUtils.isBlank(caRegistryEntry.getValue()))) {
			final CaEntity caEntity = caEntityRepository.findById(caRegistryEntry.asObjectId());
			cert = String.join("", cert,
				String.join("", caService.getCertificate(caEntity.getId().toHexString())));
		}

		return cert;
	}
}
