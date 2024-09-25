package esthesis.service.crypto.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.core.common.crypto.CryptoService;
import esthesis.core.common.crypto.dto.CertificateSignRequestDTO;
import esthesis.core.common.crypto.dto.CreateKeyPairRequestDTO;
import esthesis.service.crypto.dto.CreateCertificateRequestDTO;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.impl.repository.CaEntityRepository;
import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Authenticated
public class KeyResourceImpl implements KeyResource {

	@Inject
	@RestClient
	SettingsResource settingsResource;

	@Inject
	CryptoService cryptoService;

	@Inject
	CAService caService;

	@Inject
	CaEntityRepository caEntityRepository;

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
		CreateKeyPairRequestDTO createKeyPairRequestDTO = new CreateKeyPairRequestDTO();
		createKeyPairRequestDTO.setKeySize(
			settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE).asInt());
		createKeyPairRequestDTO.setKeyPairGeneratorAlgorithm(
			settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());

		return cryptoService.createKeyPair(createKeyPairRequestDTO);
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public String publicKeyToPEM(PublicKey publicKey) throws IOException {
		return cryptoService.publicKeyToPEM(publicKey);
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public String privateKeyToPEM(PrivateKey privateKey) throws IOException {
		return cryptoService.privateKeyToPEM(privateKey);
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
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
					cryptoService.pemToPrivateKey(caEntity.getPrivateKey(),
						settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM)
							.asString()));
		}

		// Sign the certificate.
		X509CertificateHolder x509CertificateHolder = cryptoService.generateCertificate(
			certificateSignRequestDTO);

		// Convert the certificate to PEM.
		String cert = cryptoService.certificateToPEM(x509CertificateHolder.toASN1Structure());

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
