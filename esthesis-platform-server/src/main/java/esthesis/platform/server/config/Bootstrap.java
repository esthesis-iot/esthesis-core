package esthesis.platform.server.config;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.fuse.crypto.CryptoSymmetricService;
import com.eurodyn.qlack.fuse.settings.dto.SettingDTO;
import com.eurodyn.qlack.fuse.settings.service.SettingsService;
import esthesis.common.config.AppConstants.Generic;
import esthesis.common.util.Base64E;
import esthesis.platform.server.cluster.datasinks.DataSinkManager;
import esthesis.platform.server.cluster.zookeeper.ZookeeperClientManager;
import esthesis.platform.server.config.AppConstants.ExitCodes;
import esthesis.platform.server.config.AppSettings.Setting.Provisioning;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.datasinks.DataSinkScanner;
import esthesis.platform.server.repository.CertificateRepository;
import esthesis.platform.server.service.SecurityService;
import esthesis.platform.server.service.SettingResolverService;
import javax.crypto.NoSuchPaddingException;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A bootstrap class to allow component initialization after application has fully started and all
 * Beans are properly configured (in contrast to @{@link javax.annotation.PostConstruct}). This is
 * particularly useful in case a component needs to emit events during initialization (otherwise
 * events are lost since @{@link EventListener} annotations are not fully discovered).
 *
 * In addition, bootstrapping all components in a well-defined sequence here allows greater control
 * over system's boot up times.
 */
@Component
public class Bootstrap {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(Bootstrap.class.getName());

  private final ZookeeperClientManager zookeeperClientManager;
  private final DataSinkScanner dataSinkScanner;
  private final DataSinkManager dataSinkManager;
  private final AppProperties appProperties;
  private final CryptoSymmetricService cryptoSymmetricService;
  private final SettingResolverService srs;
  private final SettingsService settingsService;
  private final SecurityService securityService;
  private final CertificateRepository certificateRepository;

  public Bootstrap(ZookeeperClientManager zookeeperClientManager, DataSinkScanner dataSinkScanner,
    DataSinkManager dataSinkManager, AppProperties appProperties,
    CryptoSymmetricService cryptoSymmetricService,
    SettingResolverService srs,
    SettingsService settingsService,
    SecurityService securityService,
    CertificateRepository certificateRepository) {
    this.zookeeperClientManager = zookeeperClientManager;
    this.dataSinkScanner = dataSinkScanner;
    this.dataSinkManager = dataSinkManager;
    this.appProperties = appProperties;
    this.cryptoSymmetricService = cryptoSymmetricService;
    this.srs = srs;
    this.settingsService = settingsService;
    this.securityService = securityService;
    this.certificateRepository = certificateRepository;
  }

  private void createAESKey(String settingName, boolean encryptValue)
  throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException,
         NoSuchPaddingException, IOException {

    final byte[] aes = cryptoSymmetricService.generateKey(
      appProperties.getSecuritySymmetricKeySize(),
      appProperties.getSecuritySymmetricKeyAlgorithm());
    LOGGER.log(Level.FINEST, "Generating AES key: {0}.", Arrays.toString(aes));
    SettingDTO settingDTO = new SettingDTO();
    settingDTO.setGroup(Generic.SYSTEM);
    settingDTO.setOwner(Generic.SYSTEM);
    settingDTO.setKey(settingName);
    settingDTO.setVal(encryptValue ? securityService.encrypt(aes) : Base64E.encode(aes));
    settingDTO.setCreatedOn(Instant.now().toEpochMilli());
    settingsService.createSetting(settingDTO);
  }

  /**
   * Generate an AES key for the platform if none is already generated.
   */
  private void generatePlatformAESKey() {
    try {
      final String key = srs.get(Security.AES_KEY);
      LOGGER.log(Level.FINEST, "Platform's AES key: " + Arrays.toString(Base64E.decode(key)));
    } catch (QDoesNotExistException e) {
      LOGGER.log(Level.CONFIG, "Platform's AES key is not set, creating one now.");
      try {
        createAESKey(Security.AES_KEY, false);
      } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
        NoSuchPaddingException | IOException ex) {
        LOGGER.log(Level.SEVERE, "Could not create an AES key for the platform, terminating.", ex);
        System.exit(ExitCodes.CANT_GENERATE_PLATFORM_AES_KEY);
      }
    }
  }

  private void generateProvisioningAESKey() {
    try {
      final String key = srs.get(Provisioning.AES_KEY);
      LOGGER.log(Level.FINEST,
        "Platform's Provisioning AES key: " + Arrays.toString(securityService.decrypt(key)));
    } catch (QDoesNotExistException | NoSuchPaddingException | InvalidAlgorithmParameterException |
      NoSuchAlgorithmException | InvalidKeyException | IOException e) {
      LOGGER.log(Level.CONFIG, "Provisioning AES key is not set, creating one now.");
      try {
        createAESKey(Provisioning.AES_KEY, true);
      } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
        NoSuchPaddingException | IOException ex) {
        LOGGER.log(Level.SEVERE, "Could not create an AES key for provisioning, terminating.", ex);
        System.exit(ExitCodes.CANT_GENERATE_PROVISIONING_AES_KEY);
      }
    }
  }

  @EventListener
  public void applicationStarted(ContextRefreshedEvent contextRefreshedEvent) {
    // Generate a key to locally encrypt data.
    generatePlatformAESKey();

    // Generate a key to shared with devices to encrypt provisioning packages.
    generateProvisioningAESKey();

    zookeeperClientManager.connect();
    dataSinkScanner.scan();
    dataSinkManager.startDataSinks();
  }
}
