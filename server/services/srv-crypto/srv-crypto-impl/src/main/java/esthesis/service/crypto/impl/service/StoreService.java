package esthesis.service.crypto.impl.service;

import com.google.common.collect.ImmutableSet;
import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Registry;
import esthesis.common.service.BaseService;
import esthesis.service.crypto.dto.Ca;
import esthesis.service.crypto.dto.Certificate;
import esthesis.service.crypto.dto.Store;
import esthesis.service.registry.resource.RegistryResourceV1;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class StoreService extends BaseService<Store> {

  public static final String KEYSTORE_TYPE = "PKCS12";
  private static final String KEYSTORE_PROVIDER = "SunJSSE";

  @Inject
  KeystoreService keystoreService;

  @Inject
  CAService caService;

  @Inject
  KeyService keyService;

  @Inject
  CertificateService certificateService;

  @Inject
  @RestClient
  RegistryResourceV1 registryResourceV1;

  public byte[] download(ObjectId id)
  throws CertificateException, KeyStoreException, NoSuchAlgorithmException,
         IOException, NoSuchProviderException, InvalidKeySpecException {
    // Get the store to download.
    final Store store = findById(id);

    // Create an empty keystore.
    byte[] keystore = keystoreService
        .createKeystore(KEYSTORE_TYPE, KEYSTORE_PROVIDER, store.getPassword());

    // Collect certificates.
    for (ObjectId certId : store.getCertCertificates()) {
      Certificate certificate = certificateService.findById(certId);
      keystore = keystoreService
          .saveCertificate(keystore, KEYSTORE_TYPE, KEYSTORE_PROVIDER,
              store.getPassword(),
              certificate.getCn(),
              certificateService.pemToCertificate(certificate.getCertificate())
                  .getEncoded());
    }

    for (ObjectId caId : store.getCertCas()) {
      Ca ca = caService.findById(caId);
      keystore = keystoreService
          .saveCertificate(keystore, KEYSTORE_TYPE, KEYSTORE_PROVIDER,
              store.getPassword(),
              ca.getCn(),
              certificateService.pemToCertificate(ca.getCertificate())
                  .getEncoded());
    }

    // Collect private keys.
    for (ObjectId certId : store.getPkCertificates()) {
      Certificate certificate = certificateService.findById(certId);
      final PrivateKey privateKey = keyService
          .pemToPrivateKey(certificate.getPrivateKey(),
              registryResourceV1.findByName(
                  Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());

      keystore = keystoreService
          .savePrivateKey(keystore, KEYSTORE_TYPE, KEYSTORE_PROVIDER,
              store.getPassword(),
              certificate.getCn(), privateKey.getEncoded(),
              registryResourceV1.findByName(
                      AppConstants.Registry.SECURITY_ASYMMETRIC_KEY_ALGORITHM)
                  .asString(),
              null, store.isPasswordForKeys() ? store.getPassword() : null,
              ImmutableSet.of(
                  certificateService.pemToCertificate(
                          certificate.getCertificate())
                      .getEncoded()));
    }

    return keystore;
  }
}
