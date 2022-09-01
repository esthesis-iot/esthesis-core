package esthesis.service.crypto.impl.resource;

import esthesis.common.AppConstants.Registry;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.crypto.dto.Ca;
import esthesis.service.crypto.dto.request.CertificateSignRequest;
import esthesis.service.crypto.dto.request.CreateCertificateRequest;
import esthesis.service.crypto.dto.request.CreateKeyPairRequest;
import esthesis.service.crypto.dto.response.CreateKeyPairResponse;
import esthesis.service.crypto.impl.repository.CaRepository;
import esthesis.service.crypto.impl.service.CertificateService;
import esthesis.service.crypto.impl.service.KeyService;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.registry.dto.RegistryEntry;
import esthesis.service.registry.resource.RegistryResource;
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
  RegistryResource registryResource;

  @Inject
  KeyService keyService;

  @Inject
  CertificateService certificateService;

  @Inject
  CaRepository caRepository;

  @Override
  public CreateKeyPairResponse generateKeyPair()
  throws NoSuchAlgorithmException, NoSuchProviderException {
    CreateKeyPairRequest createKeyPairRequestDTO = new CreateKeyPairRequest();
    createKeyPairRequestDTO.setKeySize(
        registryResource.findByName(Registry.SECURITY_ASYMMETRIC_KEY_SIZE)
            .asInt());
    createKeyPairRequestDTO
        .setKeyPairGeneratorAlgorithm(
            registryResource.findByName(
                Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());

    KeyPair keyPair = keyService.createKeyPair(createKeyPairRequestDTO);

    return new CreateKeyPairResponse()
        .setPublicKey(keyPair.getPublic().getEncoded())
        .setPrivateKey(keyPair.getPrivate().getEncoded());
  }

  @Override
  public String publicKeyToPEM(byte[] publicKey) throws IOException {
    return keyService.publicKeyToPEM(publicKey);
  }

  @Override
  public String privateKeyToPEM(byte[] privateKey) throws IOException {
    return keyService.privateKeyToPEM(privateKey);
  }

  @Override
  public String generateCertificateAsPEM(
      CreateCertificateRequest createCertificateRequest)
  throws NoSuchAlgorithmException, InvalidKeySpecException,
         OperatorCreationException, IOException {
    // Find the CA defined in the registry.
    RegistryEntry caRegistryEntry = registryResource.findByName(
        Registry.DEVICE_ROOT_CA);
    if (caRegistryEntry == null || StringUtils.isBlank(
        caRegistryEntry.getValue())) {
      throw new QDoesNotExistException(
          "The root CA is not defined in the registry.");
    }
    final Ca ca = caRepository.findById(caRegistryEntry.asObjectId());

    // Generate a certificate sign request.
    PublicKey publicKey = KeyFactory.getInstance(registryResource.findByName(
            Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
        .generatePublic(new X509EncodedKeySpec(
            createCertificateRequest.getCreateKeyPairResponse()
                .getPublicKey()));
    PrivateKey privateKey = KeyFactory.getInstance(
            registryResource.findByName(
                Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString())
        .generatePrivate(
            new PKCS8EncodedKeySpec(
                createCertificateRequest.getCreateKeyPairResponse()
                    .getPrivateKey()));
    CertificateSignRequest certificateSignRequest = new CertificateSignRequest()
        .setLocale(Locale.US)
        .setPrivateKey(privateKey)
        .setPublicKey(publicKey)
        .setSignatureAlgorithm(
            registryResource.findByName(
                Registry.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM).asString())
        .setSubjectCN(createCertificateRequest.getCn())
        .setValidForm(Instant.now())
        .setValidTo(ca.getValidity())
        .setIssuerCN(ca.getCn())
        .setIssuerPrivateKey(
            keyService.pemToPrivateKey(ca.getPrivateKey(),
                registryResource.findByName(
                    Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString()));

    // Sign the certificate.
    X509CertificateHolder x509CertificateHolder = certificateService.generateCertificate(
        certificateSignRequest);

    // Convert the certificate to PEM and return it.
    return certificateService.certificateToPEM(x509CertificateHolder);
  }

}
