package esthesis.platform.server.service;

import com.eurodyn.qlack.fuse.crypto.service.CryptoCAService;
import com.eurodyn.qlack.fuse.crypto.service.CryptoKeystoreService;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.github.slugify.Slugify;
import esthesis.platform.server.dto.DownloadReply;
import esthesis.platform.server.dto.StoreDTO;
import esthesis.platform.server.model.Certificate;
import esthesis.platform.server.model.Store;
import esthesis.platform.server.repository.CertificateRepository;
import esthesis.platform.server.repository.StoreRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

@Service
@Validated
@Transactional
public class StoreService extends BaseService<StoreDTO, Store> {

  private final static String KEYSTORE_TYPE = "PKCS12";
  private final static String KEYSTORE_PROVIDER = "SunJSSE";

  private final StoreRepository storeRepository;
  private final CertificateRepository certificateRepository;
  private final CryptoKeystoreService cryptoKeystoreService;
  private final CryptoCAService cryptoCAService;

  public StoreService(StoreRepository storeRepository,
    CertificateRepository certificateRepository,
    CryptoKeystoreService cryptoKeystoreService,
    CryptoCAService cryptoCAService) {
    this.storeRepository = storeRepository;
    this.certificateRepository = certificateRepository;
    this.cryptoKeystoreService = cryptoKeystoreService;
    this.cryptoCAService = cryptoCAService;
  }

  public DownloadReply download(long id, String keystorePassword)
  throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException, KeyStoreException,
         IOException {
    // Get the store to download.
    final Store store = ReturnOptional.r(storeRepository.findById(id));

    // Add the name of the store as the filename.
    DownloadReply downloadReply = new DownloadReply();
    downloadReply.setFilename(new Slugify().slugify(store.getName()));

    // Create an empty keystore.
    byte[] keystore = cryptoKeystoreService
      .createKeystore(KEYSTORE_TYPE, KEYSTORE_PROVIDER, keystorePassword);

    // Collect certificates.
    for (Certificate cert : store.getCertificates()) {
      keystore = cryptoKeystoreService
        .saveCertificate(keystore, KEYSTORE_TYPE, KEYSTORE_PROVIDER, keystorePassword, cert.getCn(),
          cryptoCAService.pemToCertificate(cert.getCertificate()).getEncoded());
    }

    FileUtils.writeByteArrayToFile(new File("/tmp/file1.pkcs12"), keystore);

    return null;
  }
}
