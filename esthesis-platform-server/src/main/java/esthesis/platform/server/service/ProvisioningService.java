package esthesis.platform.server.service;

import com.querydsl.core.types.Predicate;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.config.AppSettings.Setting;
import esthesis.platform.server.dto.ProvisioningDTO;
import esthesis.platform.server.mapper.ProvisioningMapper;
import esthesis.platform.server.model.Provisioning;
import esthesis.platform.server.repository.ProvisioningContentStore;
import esthesis.platform.server.repository.ProvisioningRepository;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Validated
@Transactional
public class ProvisioningService extends BaseService<ProvisioningDTO, Provisioning> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ProvisioningService.class.getName());

  private final ProvisioningMapper provisioningMapper;
  private final ProvisioningRepository provisioningRepository;
  private final SecurityService securityService;
  private final ProvisioningContentStore provisioningContentStore;
  private final AppProperties appProperties;
  private final SettingResolverService srs;

  public ProvisioningService(ProvisioningMapper provisioningMapper,
    ProvisioningRepository provisioningRepository,
    SecurityService securityService,
    ProvisioningContentStore provisioningContentStore,
    AppProperties appProperties, SettingResolverService srs) {
    this.provisioningMapper = provisioningMapper;
    this.provisioningRepository = provisioningRepository;
    this.securityService = securityService;
    this.provisioningContentStore = provisioningContentStore;
    this.appProperties = appProperties;
    this.srs = srs;
  }

  @Override
  public Page<ProvisioningDTO> findAll(Predicate predicate, Pageable pageable) {
    return provisioningMapper.map(provisioningRepository.findAll(predicate, pageable));
  }

  @Async
  public void encryptSign(long provisioningId)
  throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
         InvalidAlgorithmParameterException, NoSuchPaddingException,
         InvalidKeySpecException {
    encrypt(provisioningId);
    sign(provisioningId);
  }

  @Async
  public void sign(long provisioningId)
  throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
         SignatureException, InvalidAlgorithmParameterException, InvalidKeySpecException {
    final Provisioning provisioning = findEntityById(provisioningId);
    StopWatch stopwatch = StopWatch.createStarted();
    provisioning
      .setSignature(Base64.encodeBase64String(
        securityService.sign(provisioningContentStore.getContent(provisioning))));
    provisioningRepository.save(provisioning);
    LOGGER.log(Level.FINE, "Signing took {0} msec.", stopwatch.getTime());
  }

  @Async
  public void encrypt(long provisioningId)
  throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
         InvalidAlgorithmParameterException, IOException {
    final Provisioning provisioning = findEntityById(provisioningId);
    StopWatch stopwatch = StopWatch.createStarted();
    // Encrypt file.
    String unencryptedFile = Paths.get(appProperties.getFsProvisioningRoot(),
      provisioning.getContentId()).toFile().getAbsolutePath();
    String encryptedFile = securityService.encrypt(new File(unencryptedFile),
      new String(securityService.decrypt(srs.get(Setting.Provisioning.AES_KEY)),
        StandardCharsets.UTF_8));
    LOGGER.log(Level.FINE, "Provisioning content ID {0} was encrypted to file {1}.",
      new Object[]{provisioning.getContentId(), encryptedFile});

    // Replace unencrypted file with the encrypted one.
    try {
      FileUtils.forceDelete(new File(unencryptedFile));
      FileUtils.moveFile(new File(encryptedFile), new File(unencryptedFile));
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not replace unencrypted {0} with the encrypted {1}.",
        new Object[]{unencryptedFile, encryptedFile});
    }

    provisioning.setEncrypted(true);
    provisioningRepository.save(provisioning);

    LOGGER.log(Level.FINE, "Encryption took {0} msec.", stopwatch.getTime());
  }

  public InputStream download(long provisioningId)
  throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException,
         NoSuchPaddingException, IOException {
    final Provisioning provisioning = findEntityById(provisioningId);

    // Decrypt the provisioning package if encrypted.
    if (provisioning.isEncrypted()) {
      String encryptedFile = Paths.get(appProperties.getFsProvisioningRoot(),
        provisioning.getContentId()).toFile().getAbsolutePath();
      String decryptedFile = securityService.decrypt(new File(encryptedFile),
        new String(securityService.decrypt(srs.get(Setting.Provisioning.AES_KEY)),
          StandardCharsets.UTF_8));
      LOGGER.log(Level.FINE, "A temporary decrypted version of content ID {0} was created under "
        + "{1}.", new Object[]{provisioning.getContentId(), decryptedFile});
      return new FileInputStream(new File(decryptedFile));
    } else {
      return provisioningContentStore.getContent(provisioning);
    }
  }

  public long save(ProvisioningDTO provisioningDTO, MultipartFile file) throws IOException {
    return super.save(provisioningDTO, file.getInputStream()).getId();
  }

}
