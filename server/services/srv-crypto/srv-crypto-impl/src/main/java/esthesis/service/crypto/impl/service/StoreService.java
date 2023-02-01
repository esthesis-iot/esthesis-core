package esthesis.service.crypto.impl.service;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.entity.StoreEntity;
import esthesis.service.settings.resource.SettingsResource;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class StoreService extends BaseService<StoreEntity> {

  public static final String KEYSTORE_TYPE = "PKCS12";
  private static final String KEYSTORE_PROVIDER = "SunJSSE";

  @Inject
  CAService caService;

  @Inject
  CryptoService cryptoService;

  @Inject
  CertificateService certificateService;

  @Inject
  @RestClient
  SettingsResource settingsResource;

  public byte[] download(String id)
  throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException,
         NoSuchProviderException, InvalidKeySpecException {
    // Get the store to download.
    final StoreEntity storeEntity = findById(id);

    // Create an empty keystore.
    byte[] keystore = cryptoService.createKeystore(KEYSTORE_TYPE, KEYSTORE_PROVIDER,
        storeEntity.getPassword());

    // Collect certificates.
    if (CollectionUtils.isNotEmpty(storeEntity.getCertCertificates())) {
      for (ObjectId certId : storeEntity.getCertCertificates()) {
        CertificateEntity certificateEntity = certificateService.findById(certId.toHexString());
        keystore = cryptoService.saveCertificateToKeystore(keystore, KEYSTORE_TYPE,
            KEYSTORE_PROVIDER,
            storeEntity.getPassword(), certificateEntity.getCn(),
            cryptoService.pemToCertificate(certificateEntity.getCertificate()).getEncoded());
      }
    }

    if (CollectionUtils.isNotEmpty(storeEntity.getCertCas())) {
      for (ObjectId caId : storeEntity.getCertCas()) {
        CaEntity caEntity = caService.findById(caId.toHexString());
        keystore = cryptoService.saveCertificateToKeystore(keystore, KEYSTORE_TYPE,
            KEYSTORE_PROVIDER,
            storeEntity.getPassword(), caEntity.getCn(),
            cryptoService.pemToCertificate(caEntity.getCertificate()).getEncoded());
      }
    }

    // Collect private keys.
    if (CollectionUtils.isNotEmpty(storeEntity.getPkCertificates())) {
      for (ObjectId certId : storeEntity.getPkCertificates()) {
        CertificateEntity certificateEntity = certificateService.findById(certId.toHexString());
        final PrivateKey privateKey = cryptoService.pemToPrivateKey(
            certificateEntity.getPrivateKey(),
            settingsResource.findByName(NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString());

        keystore = cryptoService.savePrivateKeyToKeystore(keystore, KEYSTORE_TYPE,
            KEYSTORE_PROVIDER,
            storeEntity.getPassword(), certificateEntity.getCn(), privateKey,
            storeEntity.isPasswordForKeys() ? storeEntity.getPassword() : null,
            Set.of(
                cryptoService.pemToCertificate(certificateEntity.getCertificate()).getEncoded()));
      }
    }

    return keystore;
  }
}
