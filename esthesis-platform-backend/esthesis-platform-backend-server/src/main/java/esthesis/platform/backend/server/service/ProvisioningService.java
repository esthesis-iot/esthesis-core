package esthesis.platform.backend.server.service;

import com.eurodyn.qlack.fuse.crypto.service.CryptoDigestService;
import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.common.device.dto.DeviceDTO;
import esthesis.platform.backend.common.util.Base64E;
import esthesis.platform.backend.server.config.AppProperties;
import esthesis.platform.backend.server.config.AppSettings.Setting;
import esthesis.platform.backend.server.dto.ProvisioningDTO;
import esthesis.platform.backend.server.mapper.ProvisioningMapper;
import esthesis.platform.backend.server.model.Provisioning;
import esthesis.platform.backend.server.repository.ProvisioningContentStore;
import esthesis.platform.backend.server.repository.ProvisioningRepository;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Comparator;
import java.util.Optional;
import java.util.logging.Level;

@Log
@Service
@Validated
@Transactional
public class ProvisioningService extends BaseService<ProvisioningDTO, Provisioning> {

  private final ProvisioningMapper provisioningMapper;
  private final ProvisioningRepository provisioningRepository;
  private final SecurityService securityService;
  private final ProvisioningContentStore provisioningContentStore;
  private final AppProperties appProperties;
  private final SettingResolverService srs;
  private final CryptoDigestService cryptoDigestService;

  public ProvisioningService(ProvisioningMapper provisioningMapper,
    ProvisioningRepository provisioningRepository,
    SecurityService securityService,
    ProvisioningContentStore provisioningContentStore,
    AppProperties appProperties, SettingResolverService srs,
    CryptoDigestService cryptoDigestService) {
    this.provisioningMapper = provisioningMapper;
    this.provisioningRepository = provisioningRepository;
    this.securityService = securityService;
    this.provisioningContentStore = provisioningContentStore;
    this.appProperties = appProperties;
    this.srs = srs;
    this.cryptoDigestService = cryptoDigestService;
  }

  @Override
  public Page<ProvisioningDTO> findAll(Predicate predicate, Pageable pageable) {
    return provisioningMapper.map(provisioningRepository.findAll(predicate, pageable));
  }

  @Async
  public void encryptAndSign(long provisioningId)
  throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
         NoSuchPaddingException, SignatureException, InvalidKeyException, InvalidKeySpecException {
    final Provisioning provisioning = findEntityById(provisioningId);
    StopWatch stopWatch = StopWatch.createStarted();
    // First sign the non-encrypted version.
    provisioning.setSignaturePlain(Base64E.encode(
      securityService.sign(provisioningContentStore.getContent(provisioning))));

    // Create the encrypted version.
    String plaintextFilePath = Paths
      .get(appProperties.getFsProvisioningRoot(), provisioning.getContentId()).toFile()
      .getAbsolutePath();
    String encryptedFile = securityService.encrypt(new File(plaintextFilePath),
      new File(plaintextFilePath + ".encrypted"),
      Base64E.encode(securityService.decrypt(srs.get(Setting.Provisioning.AES_KEY))));

    log.log(Level.FINE, "Provisioning content ID {0} was encrypted to file {1}.",
      new Object[]{provisioning.getContentId(), encryptedFile});
    provisioning.setEncrypted(true);

    // Sign the encrypted version.
    try (FileInputStream payload = new FileInputStream(encryptedFile)) {
      provisioning
        .setSignatureEncrypted(Base64E.encode(securityService.sign(payload)));
    }

    // Calculate a digest of the unencrypted file.
    provisioning
      .setSha256(cryptoDigestService.sha256(provisioningContentStore.getContent(provisioning)));

    provisioningRepository.save(provisioning);
    log.log(Level.FINE, "Encryption and signing took {0} msec.", stopWatch.getTime());
  }

  public InputStream download(long provisioningId, boolean fetchEncryptedVersion)
  throws IOException {
    final Provisioning provisioning = findEntityById(provisioningId);
    if (fetchEncryptedVersion) {
      return new FileInputStream(Paths
        .get(appProperties.getFsProvisioningRoot(), provisioning.getContentId() + ".encrypted")
        .toFile().getAbsolutePath());
    } else {
      return provisioningContentStore.getContent(provisioning);
    }
  }

  public long save(ProvisioningDTO provisioningDTO, MultipartFile file) throws IOException {
    return super.save(provisioningDTO, file.getInputStream()).getId();
  }

  public Optional<ProvisioningDTO> matchByTag(DeviceDTO deviceDTO) {
    Optional<ProvisioningDTO> provisioningDTO;

    if (CollectionUtils.isEmpty(deviceDTO.getTags())) {
      provisioningDTO = findAll().stream()
        .filter(ProvisioningDTO::isState)
        .filter(o -> o.getTags().isEmpty())
        .max(Comparator.comparing(ProvisioningDTO::getPackageVersion));
    } else {
      provisioningDTO = findAll().stream()
        .filter(ProvisioningDTO::isState)
        .filter(o ->
          CollectionUtils.intersection(deviceDTO.getTags(), o.getTags()).size() ==
            deviceDTO.getTags().size())
        .max(Comparator.comparing(ProvisioningDTO::getPackageVersion));
    }

    return provisioningDTO;
  }

  @Override
  public ProvisioningDTO deleteById(long id) {
    String contentId = findEntityById(id).getContentId();
    final Path encryptedFile = Paths
      .get(appProperties.getFsProvisioningRoot(), contentId + ".encrypted");
    try {
      Files.deleteIfExists(encryptedFile);
    } catch (IOException e) {
      log.log(Level.SEVERE, "Could not delete encrypted provisioning file: {0}.",
        encryptedFile.toAbsolutePath().toString());
    }
    return super.deleteById(id);
  }
}
