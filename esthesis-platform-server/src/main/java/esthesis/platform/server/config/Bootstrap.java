package esthesis.platform.server.config;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.fuse.crypto.CryptoSymmetricService;
import com.eurodyn.qlack.fuse.settings.dto.SettingDTO;
import com.eurodyn.qlack.fuse.settings.service.SettingsService;
import esthesis.extension.config.AppConstants.Generic;
import esthesis.platform.server.cluster.datasinks.DataSinkManager;
import esthesis.platform.server.cluster.zookeeper.ZookeeperClientManager;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.datasinks.DataSinkScanner;
import esthesis.platform.server.service.SettingResolverService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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

  public Bootstrap(ZookeeperClientManager zookeeperClientManager, DataSinkScanner dataSinkScanner,
    DataSinkManager dataSinkManager, AppProperties appProperties,
    CryptoSymmetricService cryptoSymmetricService,
    SettingResolverService srs,
    SettingsService settingsService) {
    this.zookeeperClientManager = zookeeperClientManager;
    this.dataSinkScanner = dataSinkScanner;
    this.dataSinkManager = dataSinkManager;
    this.appProperties = appProperties;
    this.cryptoSymmetricService = cryptoSymmetricService;
    this.srs = srs;
    this.settingsService = settingsService;
  }

  /**
   * Generate an AES key for the platform if none is already generated.
   */
  private void generateAESKey() {
    try {
      srs.get(Security.AES_KEY);
    } catch (QDoesNotExistException e) {
      LOGGER.log(Level.CONFIG, "Platform's AES key is not set, creating one now.");
      try {
        String aesKey = Base64.encodeBase64String(
          cryptoSymmetricService.generateKey(appProperties.getSecuritySymmetricKeySize(),
            appProperties.getSecuritySymmetricKeyAlgorithm()));
        SettingDTO settingDTO = new SettingDTO();
        settingDTO.setGroup(Generic.SYSTEM);
        settingDTO.setOwner(Generic.SYSTEM);
        settingDTO.setKey(Security.AES_KEY);
        settingDTO.setVal(aesKey);
        settingDTO.setCreatedOn(Instant.now().toEpochMilli());
        settingsService.createSetting(settingDTO);
      } catch (NoSuchAlgorithmException ex) {
        LOGGER.log(Level.SEVERE, "Could not create an AES key for the platform, terminating.");
        System.exit(1);
      }
    }
  }

  @EventListener
  public void applicationStarted(ContextRefreshedEvent contextRefreshedEvent) {
    generateAESKey();
    zookeeperClientManager.connect();
    dataSinkScanner.scan();
    dataSinkManager.startDataSinks();
  }
}
