package esthesis.service.crypto.impl.resource;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.crypto.dto.CertificateSignRequestDTO;
import esthesis.service.crypto.dto.CreateCertificateRequestDTO;
import esthesis.service.crypto.dto.CreateKeyPairRequestDTO;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.impl.repository.CaEntityRepository;
import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.impl.service.CryptoService;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.security.Authenticated;
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
import java.util.Locale;
import javax.inject.Inject;
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
  public KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
    CreateKeyPairRequestDTO createKeyPairRequestDTO = new CreateKeyPairRequestDTO();
    createKeyPairRequestDTO.setKeySize(
        settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_SIZE).asInt());
    createKeyPairRequestDTO.setKeyPairGeneratorAlgorithm(
        settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());

    return cryptoService.createKeyPair(createKeyPairRequestDTO);
  }

  @Override
  public String publicKeyToPEM(PublicKey publicKey) throws IOException {
    return cryptoService.publicKeyToPEM(publicKey);
  }

  @Override
  public String privateKeyToPEM(PrivateKey privateKey) throws IOException {
    return cryptoService.privateKeyToPEM(privateKey);
  }

  @Override
  public String generateCertificateAsPEM(CreateCertificateRequestDTO createCertificateRequestDTO)
  throws NoSuchAlgorithmException, InvalidKeySpecException, OperatorCreationException, IOException {
    // Find the CA defined in the registry.
    SettingEntity caRegistryEntry = settingsResource.findByName(NamedSetting.DEVICE_ROOT_CA);
    if (caRegistryEntry == null || StringUtils.isBlank(caRegistryEntry.getValue())) {
      throw new QDoesNotExistException("The root CA is not defined in the registry.");
    }
    final CaEntity caEntity = caEntityRepository.findById(caRegistryEntry.asObjectId());

    // Generate a certificate sign request.
    PublicKey publicKey = KeyFactory.getInstance(
            settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
        .generatePublic(new X509EncodedKeySpec(
            createCertificateRequestDTO.getKeyPair().getPublic().getEncoded()));
    PrivateKey privateKey = KeyFactory.getInstance(
            settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
        .generatePrivate(new PKCS8EncodedKeySpec(
            createCertificateRequestDTO.getKeyPair().getPrivate().getEncoded()));
    CertificateSignRequestDTO certificateSignRequestDTO = new CertificateSignRequestDTO().setLocale(
            Locale.getDefault()).setPrivateKey(privateKey).setPublicKey(publicKey)
        .setSignatureAlgorithm(
            settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM)
                .asString()).setSubjectCN(createCertificateRequestDTO.getCn())
        .setValidForm(Instant.now()).setValidTo(caEntity.getValidity())
        .setIssuerCN(caEntity.getCn()).setIssuerPrivateKey(
            cryptoService.pemToPrivateKey(caEntity.getPrivateKey(),
                settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM)
                    .asString()));

    // Sign the certificate.
    X509CertificateHolder x509CertificateHolder = cryptoService.generateCertificate(
        certificateSignRequestDTO);

    // Convert the certificate to PEM.
    String cert = cryptoService.certificateToPEM(x509CertificateHolder.toASN1Structure());

    // Add certificate chain, if requested.
    if (createCertificateRequestDTO.isIncludeCertificateChain()) {
      cert = String.join("", cert,
          String.join("", caService.getCertificate(caEntity.getId().toHexString())));
    }

    return cert;
  }

}
