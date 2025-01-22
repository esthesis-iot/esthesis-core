package esthesis.service.crypto.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.crypto.dto.CreateCertificateRequestDTO;
import esthesis.service.crypto.impl.dto.CreateKeyPairRequestDTO;
import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.impl.service.CertificateService;
import esthesis.service.crypto.impl.util.CryptoConvertersUtil;
import esthesis.service.crypto.impl.util.CryptoUtil;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Authenticated
public class KeyResourceImpl implements KeyResource {

	@Inject
	@RestClient
	SettingsResource settingsResource;

	@Inject
	CAService caService;
	@Inject
	CertificateService certificateService;

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
		CreateKeyPairRequestDTO createKeyPairRequestDTO = new CreateKeyPairRequestDTO();
		createKeyPairRequestDTO.setKeySize(
			settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE).asInt());
		createKeyPairRequestDTO.setKeyPairGeneratorAlgorithm(
			settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());

		return CryptoUtil.createKeyPair(createKeyPairRequestDTO);
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public String publicKeyToPEM(PublicKey publicKey) throws IOException {
		return CryptoConvertersUtil.publicKeyToPEM(publicKey);
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public String privateKeyToPEM(PrivateKey privateKey) throws IOException {
		return CryptoConvertersUtil.privateKeyToPEM(privateKey);
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public String generateCertificateAsPEM(CreateCertificateRequestDTO createCertificateRequestDTO)
	throws NoSuchAlgorithmException, InvalidKeySpecException, OperatorCreationException, IOException {
		return certificateService.generateCertificateAsPEM(createCertificateRequestDTO);
	}
}
