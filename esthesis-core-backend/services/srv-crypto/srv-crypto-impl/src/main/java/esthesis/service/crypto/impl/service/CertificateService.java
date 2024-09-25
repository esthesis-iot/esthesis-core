package esthesis.service.crypto.impl.service;

import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.CRYPTO;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;

import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.core.common.crypto.CryptoService;
import esthesis.core.common.crypto.dto.CertificateSignRequestDTO;
import esthesis.core.common.crypto.dto.CreateKeyPairRequestDTO;
import esthesis.core.common.exception.QCouldNotSaveException;
import esthesis.core.common.exception.QMismatchException;
import esthesis.core.common.exception.QMutationNotPermittedException;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import esthesis.util.kafka.notifications.outgoing.KafkaNotification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Slf4j
@Transactional
@ApplicationScoped
public class CertificateService extends BaseService<CertificateEntity> {

	@Inject
	CAService caService;

	@Inject
	CryptoService cryptoService;

	@Inject
	@RestClient
	SettingsResource settingsResource;

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
			X509Certificate x509Certificate = cryptoService.pemToCertificate(
				certificateEntity.getCertificate());
			certificateEntity.setCn(
				cryptoService.cleanUpCn(x509Certificate.getSubjectX500Principal().getName()));
			certificateEntity.setIssued(x509Certificate.getNotBefore().toInstant());
			certificateEntity.setValidity(x509Certificate.getNotAfter().toInstant());
			certificateEntity.setIssuer(
				cryptoService.cleanUpCn(x509Certificate.getIssuerX500Principal().getName()));
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
			final KeyPair keyPair = cryptoService.createKeyPair(
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
					cryptoService.pemToPrivateKey(
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
				cryptoService.generateCertificate(certificateSignRequestDTO);

			// Generate the PEM version of the certificate.
			String certPEM = cryptoService.certificateToPEM(x509CertificateHolder.toASN1Structure());

			// Add the certificate chain.
			if (caEntity != null) {
				certPEM = certPEM + String.join("",
					caService.getCertificate(caEntity.getId().toHexString()));
			}

			// Populate the certificate DTO to persist it.
			certificateEntity.setIssued(certificateSignRequestDTO.getValidForm());
			certificateEntity
				.setPrivateKey(cryptoService.privateKeyToPEM(keyPair.getPrivate()));
			certificateEntity.setPublicKey(cryptoService.publicKeyToPEM(keyPair.getPublic()));
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

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public String getPrivateKey(String certId) {
		CertificateEntity certificateEntity = findById(certId);

		return certificateEntity.getPrivateKey();
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public String getPublicKey(String certId) {
		CertificateEntity certificateEntity = findById(certId);

		return certificateEntity.getPublicKey();
	}

	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public String getCertificate(String certId) {
		CertificateEntity certificateEntity = findById(certId);

		return certificateEntity.getCertificate();
	}

	@Override
	@KafkaNotification(component = Component.CERTIFICATE, subject = Subject.CERTIFICATE,
		action = Action.DELETE, idParamOrder = 0)
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = DELETE)
	public boolean deleteById(String id) {
		return super.deleteById(id);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public CertificateEntity findById(String id) {
		return super.findById(id);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = CRYPTO, operation = READ)
	public Page<CertificateEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}
}
