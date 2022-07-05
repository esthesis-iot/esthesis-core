package esthesis.service.crypto.impl.resource;

import esthesis.common.util.AppConstants.Registry;
import esthesis.service.crypto.dto.Ca;
import esthesis.service.crypto.dto.CertificateRequest;
import esthesis.service.crypto.dto.KeyPairResponse;
import esthesis.service.crypto.impl.dto.CSR;
import esthesis.service.crypto.impl.dto.CreateKeyPair;
import esthesis.service.crypto.impl.repository.CaRepository;
import esthesis.service.crypto.impl.service.CryptoAsymmetricService;
import esthesis.service.crypto.impl.service.CryptoCAService;
import esthesis.service.crypto.resource.CryptoResourceV1;
import esthesis.service.registry.resource.RegistryResourceV1;
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
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

public class CryptoResourceV1Impl implements CryptoResourceV1 {

  @Inject
  @RestClient
  RegistryResourceV1 registryResourceV1;

  @Inject
  CryptoAsymmetricService cryptoAsymmetricService;

  @Inject
  CryptoCAService cryptoCAService;

  @Inject
  CaRepository caRepository;

  @Override
  public KeyPairResponse generateKeyPair()
  throws NoSuchAlgorithmException, NoSuchProviderException {
    CreateKeyPair createKeyPairDTO = new CreateKeyPair();
    createKeyPairDTO.setKeySize(
        registryResourceV1.findByName(Registry.SECURITY_ASYMMETRIC_KEY_SIZE)
            .asInt());
    createKeyPairDTO
        .setKeyPairGeneratorAlgorithm(
            registryResourceV1.findByName(
                Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());

    KeyPair keyPair = cryptoAsymmetricService.createKeyPair(createKeyPairDTO);

    return new KeyPairResponse()
        .setPublicKey(keyPair.getPublic().getEncoded())
        .setPrivateKey(keyPair.getPrivate().getEncoded());
  }

  @Override
  public String publicKeyToPEM(byte[] publicKey) throws IOException {
    return cryptoAsymmetricService.publicKeyToPEM(publicKey);
  }

  @Override
  public String privateKeyToPEM(byte[] privateKey) throws IOException {
    return cryptoAsymmetricService.privateKeyToPEM(privateKey);
  }

  @Override
  public String generateCertificateAsPEM(CertificateRequest certificateRequest)
  throws NoSuchAlgorithmException, InvalidKeySpecException,
         OperatorCreationException, IOException {
    // Find the CA defined in the registry.
    final Ca ca = caRepository.findById(
        registryResourceV1.findByName(Registry.DEVICE_ROOT_CA).asObjectId());

    // Generate a certificate sign request.
    PublicKey publicKey = KeyFactory.getInstance(registryResourceV1.findByName(
            Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
        .generatePublic(new X509EncodedKeySpec(
            certificateRequest.getKeyPairResponse().getPublicKey()));
    PrivateKey privateKey = KeyFactory.getInstance(
            registryResourceV1.findByName(
                Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
        .generatePrivate(
            new PKCS8EncodedKeySpec(
                certificateRequest.getKeyPairResponse().getPrivateKey()));
    KeyPair keyPair = new KeyPair(publicKey, privateKey);
    CSR CSRRequest = new CSR()
        .setLocale(Locale.US)
        .setPrivateKey(privateKey)
        .setPublicKey(publicKey)
        .setSignatureAlgorithm(
            registryResourceV1.findByName(
                Registry.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM).asString())
        .setSubjectCN(certificateRequest.getCn())
        .setValidForm(Instant.now())
        .setValidTo(ca.getValidity())
        .setIssuerCN(ca.getCn())
        .setIssuerPrivateKey(
            cryptoAsymmetricService.pemToPrivateKey(ca.getPrivateKey(),
                registryResourceV1.findByName(
                    Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString()));

    // Sign the certificate.
    X509CertificateHolder x509CertificateHolder = cryptoCAService.generateCertificate(
        CSRRequest);

    // Convert the certificate to PEM and return it.
    return cryptoCAService.certificateToPEM(x509CertificateHolder);
  }
}
