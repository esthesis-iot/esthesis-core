package esthesis.platform.backend.server.service;

import com.eurodyn.qlack.fuse.crypto.service.CryptoAsymmetricService;
import com.eurodyn.qlack.fuse.crypto.service.CryptoCAService;
import com.eurodyn.qlack.fuse.crypto.service.CryptoKeystoreService;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.github.slugify.Slugify;
import com.google.common.collect.ImmutableSet;
import esthesis.platform.backend.server.config.AppProperties;
import esthesis.platform.backend.server.dto.DownloadReply;
import esthesis.platform.backend.server.dto.StoreDTO;
import esthesis.platform.backend.server.model.Ca;
import esthesis.platform.backend.server.model.Certificate;
import esthesis.platform.backend.server.model.Store;
import esthesis.platform.backend.server.repository.StoreRepository;
import javax.crypto.NoSuchPaddingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

@Service
@Validated
@Transactional
public class StoreService extends BaseService<StoreDTO, Store> {

  private static final String KEYSTORE_TYPE = "PKCS12";
  private static final String KEYSTORE_PROVIDER = "SunJSSE";

  private final StoreRepository storeRepository;
  private final CryptoKeystoreService cryptoKeystoreService;
  private final CryptoCAService cryptoCAService;
  private final CryptoAsymmetricService cryptoAsymmetricService;
  private final AppProperties appProperties;
  private final SecurityService securityService;

  public StoreService(StoreRepository storeRepository,
    CryptoKeystoreService cryptoKeystoreService,
    CryptoCAService cryptoCAService,
    CryptoAsymmetricService cryptoAsymmetricService,
    AppProperties appProperties, SecurityService securityService) {
    this.storeRepository = storeRepository;
    this.cryptoKeystoreService = cryptoKeystoreService;
    this.cryptoCAService = cryptoCAService;
    this.cryptoAsymmetricService = cryptoAsymmetricService;
    this.appProperties = appProperties;
    this.securityService = securityService;
  }

  public DownloadReply download(long id)
  throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException, KeyStoreException,
         IOException, InvalidKeySpecException, NoSuchPaddingException,
         InvalidAlgorithmParameterException, InvalidKeyException {
    // Get the store to download.
    final Store store = ReturnOptional.r(storeRepository.findById(id));

    // Add the name of the store as the filename.
    DownloadReply downloadReply = new DownloadReply();
    downloadReply
      .setFilename(new Slugify().slugify(store.getName()) + "." + KEYSTORE_TYPE.toLowerCase());

    // Create an empty keystore.
    byte[] keystore = cryptoKeystoreService
      .createKeystore(KEYSTORE_TYPE, KEYSTORE_PROVIDER, store.getPassword());

    // Collect certificates.
    for (Certificate cert : store.getCertCertificates()) {
      keystore = cryptoKeystoreService
        .saveCertificate(keystore, KEYSTORE_TYPE, KEYSTORE_PROVIDER, store.getPassword(),
          cert.getCn(), cryptoCAService.pemToCertificate(cert.getCertificate()).getEncoded());
    }
    for (Ca ca : store.getCertCas()) {
      keystore = cryptoKeystoreService
        .saveCertificate(keystore, KEYSTORE_TYPE, KEYSTORE_PROVIDER, store.getPassword(),
          ca.getCn(), cryptoCAService.pemToCertificate(ca.getCertificate()).getEncoded());
    }

    // Collect private keys.
    for (Certificate cert : store.getPkCertificates()) {
      final PrivateKey privateKey = cryptoAsymmetricService
        .pemToPrivateKey(new String(securityService.decrypt(cert.getPrivateKey())),
          appProperties.getSecurityAsymmetricKeyAlgorithm());

      keystore = cryptoKeystoreService
        .savePrivateKey(keystore, KEYSTORE_TYPE, KEYSTORE_PROVIDER, store.getPassword(),
          cert.getCn(), privateKey.getEncoded(),
          appProperties.getSecurityAsymmetricKeyAlgorithm(),
          null, store.isPasswordForKeys() ? store.getPassword() : null,
          ImmutableSet.of(cryptoCAService.pemToCertificate(cert.getCertificate()).getEncoded()));
    }

    downloadReply.setBinaryPayload(keystore);

    return downloadReply;
  }
}
