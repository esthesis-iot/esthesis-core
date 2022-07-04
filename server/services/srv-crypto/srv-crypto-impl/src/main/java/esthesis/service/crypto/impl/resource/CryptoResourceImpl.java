package esthesis.service.crypto.impl.resource;

import esthesis.common.util.AppConstants.Registry;
import esthesis.service.crypto.dto.Ca;
import esthesis.service.crypto.dto.CertificateRequest;
import esthesis.service.crypto.impl.dto.CertificateSign;
import esthesis.service.crypto.impl.dto.CreateKeyPair;
import esthesis.service.crypto.impl.repository.CaRepository;
import esthesis.service.crypto.impl.service.CryptoAsymmetricService;
import esthesis.service.crypto.impl.service.CryptoCAService;
import esthesis.service.crypto.resource.CryptoResource;
import esthesis.service.registry.resource.RegistryResource;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Locale;
import javax.inject.Inject;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

public class CryptoResourceImpl implements CryptoResource {

  @Inject
  @RestClient
  RegistryResource registryResource;

  @Inject
  CryptoAsymmetricService cryptoAsymmetricService;

  @Inject
  CryptoCAService cryptoCAService;

  @Inject
  CaRepository caRepository;

  @Override
  public KeyPair generateKeyPair()
  throws NoSuchAlgorithmException, NoSuchProviderException {
    CreateKeyPair createKeyPairDTO = new CreateKeyPair();
    createKeyPairDTO.setKeySize(
        registryResource.findByName(Registry.SECURITY_ASYMMETRIC_KEY_SIZE)
            .asInt());
    createKeyPairDTO
        .setKeyPairGeneratorAlgorithm(
            registryResource.findByName(
                Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());

    return cryptoAsymmetricService.createKeyPair(createKeyPairDTO);
  }

  @Override
  public String publicKeyToPEM(KeyPair keyPair) throws IOException {
    return cryptoAsymmetricService.publicKeyToPEM(keyPair);
  }

  @Override
  public String privateKeyToPEM(KeyPair keyPair) throws IOException {
    return cryptoAsymmetricService.privateKeyToPEM(keyPair);
  }

  @Override
  public String generateCertificateAsPEM(CertificateRequest certificateRequest)
  throws NoSuchAlgorithmException, InvalidKeySpecException,
         OperatorCreationException, IOException {
    // Find the CA defined in the registry.
    final Ca ca = caRepository.findById(
        registryResource.findByName(Registry.DEVICE_ROOT_CA).asObjectId());

    // Generate a certificate sign request.
    CertificateSign certificateSignRequest = new CertificateSign()
        .setLocale(Locale.US)
        .setPrivateKey(certificateRequest.getKeyPair().getPrivate())
        .setPublicKey(certificateRequest.getKeyPair().getPublic())
        .setSignatureAlgorithm(
            registryResource.findByName(
                Registry.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM).asString())
        .setSubjectCN(certificateRequest.getCn())
        .setValidForm(Instant.now())
        .setValidTo(ca.getValidity())
        .setIssuerCN(ca.getCn())
        .setIssuerPrivateKey(
            cryptoAsymmetricService.pemToPrivateKey(ca.getPrivateKey(),
                registryResource.findByName(
                    Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString()));

    // Sign the certificate.
    X509CertificateHolder x509CertificateHolder = cryptoCAService.generateCertificate(
        certificateSignRequest);

    // Convert the certificate to PEM and return it.
    return cryptoCAService.certificateToPEM(x509CertificateHolder);
  }
}
