package esthesis.platform.server.config;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.fuse.crypto.CryptoSymmetricService;
import com.eurodyn.qlack.fuse.settings.dto.SettingDTO;
import com.eurodyn.qlack.fuse.settings.service.SettingsService;
import esthesis.extension.config.AppConstants.Generic;
import esthesis.platform.server.cluster.datasinks.DataSinkManager;
import esthesis.platform.server.cluster.zookeeper.ZookeeperClientManager;
import esthesis.platform.server.config.AppConstants.ExitCodes;
import esthesis.platform.server.config.AppSettings.Setting.Provisioning;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.datasinks.DataSinkScanner;
import esthesis.platform.server.service.SecurityService;
import esthesis.platform.server.service.SettingResolverService;
import internal.org.springframework.content.fs.boot.autoconfigure.FilesystemContentAutoConfiguration;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
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
  private final FilesystemContentAutoConfiguration filesystemContentAutoConfiguration;

  public Bootstrap(ZookeeperClientManager zookeeperClientManager, DataSinkScanner dataSinkScanner,
    DataSinkManager dataSinkManager, AppProperties appProperties,
    CryptoSymmetricService cryptoSymmetricService,
    SettingResolverService srs,
    SettingsService settingsService,
    SecurityService securityService,
    FilesystemContentAutoConfiguration filesystemContentAutoConfiguration) {
    this.zookeeperClientManager = zookeeperClientManager;
    this.dataSinkScanner = dataSinkScanner;
    this.dataSinkManager = dataSinkManager;
    this.appProperties = appProperties;
    this.cryptoSymmetricService = cryptoSymmetricService;
    this.srs = srs;
    this.settingsService = settingsService;
    this.securityService = securityService;
    this.filesystemContentAutoConfiguration = filesystemContentAutoConfiguration;
  }

  private void createAESKey(String settingName, boolean encryptValue)
  throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException,
         NoSuchPaddingException, IOException {
    String aesKey = Base64.encodeBase64String(
      cryptoSymmetricService.generateKey(
        appProperties.getSecuritySymmetricKeySize(),
        appProperties.getSecuritySymmetricKeyAlgorithm()));
    SettingDTO settingDTO = new SettingDTO();
    settingDTO.setGroup(Generic.SYSTEM);
    settingDTO.setOwner(Generic.SYSTEM);
    settingDTO.setKey(settingName);
    settingDTO.setVal(encryptValue ? securityService.encrypt(aesKey) : aesKey);
    settingDTO.setCreatedOn(Instant.now().toEpochMilli());
    settingsService.createSetting(settingDTO);
  }

  /**
   * Generate an AES key for the platform if none is already generated.
   */
  private void generatePlatformAESKey() {
    try {
      srs.get(Security.AES_KEY);
    } catch (QDoesNotExistException e) {
      LOGGER.log(Level.CONFIG, "Platform's AES key is not set, creating one now.");
      try {
        createAESKey(Security.AES_KEY, false);
      } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
        NoSuchPaddingException | IOException ex) {
        LOGGER.log(Level.SEVERE, "Could not create an AES key for the platform, terminating.");
        System.exit(ExitCodes.CANT_GENERATE_PLATFORM_AES_KEY);
      }
    }
  }

  private void generateProvisioningAESKey() {
    try {
      srs.get(Provisioning.AES_KEY);
    } catch (QDoesNotExistException e) {
      LOGGER.log(Level.CONFIG, "Provisioning AES key is not set, creating one now.");
      try {
        createAESKey(Provisioning.AES_KEY, true);
      } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
        NoSuchPaddingException | IOException ex) {
        LOGGER.log(Level.SEVERE, "Could not create an AES key for provisioning, terminating.");
        System.exit(ExitCodes.CANT_GENERATE_PROVISIONING_AES_KEY);
      }
    }
  }

  @EventListener
  public void applicationStarted(ContextRefreshedEvent contextRefreshedEvent) {
    // Generate a key to locally encrypt data.
    generatePlatformAESKey();

    // Generate a shared key to encrypt provisioning packages.
    generateProvisioningAESKey();

    zookeeperClientManager.connect();
    dataSinkScanner.scan();
    dataSinkManager.startDataSinks();
  }
}
