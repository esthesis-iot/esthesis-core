package esthesis.backend.config;

import com.eurodyn.qlack.fuse.crypto.service.CryptoSymmetricService;
import com.eurodyn.qlack.fuse.settings.dto.SettingDTO;
import com.eurodyn.qlack.fuse.settings.service.SettingsService;
import esthesis.backend.config.AppConstants.ExitCodes;
import esthesis.backend.config.AppSettings.Setting.Provisioning;
import esthesis.backend.config.AppSettings.Setting.Security;
import esthesis.backend.service.SecurityService;
import esthesis.common.config.AppConstants.Generic;
import esthesis.common.util.Base64E;
import esthesis.backend.service.SettingResolverService;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.java.Log;
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

/**
 * A bootstrap class to allow component initialization after application has fully started and all
 * Beans are properly configured (in contrast to @{@link javax.annotation.PostConstruct}). This is
 * particularly useful in case a component needs to emit events during initialization (otherwise
 * events are lost since @{@link EventListener} annotations are not fully discovered).
 *
 * In addition, bootstrapping all components in a well-defined sequence here allows greater control
 * over system's boot up times.
 */
@Log
@Component
public class Bootstrap {

  private final AppProperties appProperties;
  private final CryptoSymmetricService cryptoSymmetricService;
  private final SettingResolverService srs;
  private final SettingsService settingsService;
  private final SecurityService securityService;

  public Bootstrap(AppProperties appProperties,
    CryptoSymmetricService cryptoSymmetricService,
    SettingResolverService srs,
    SettingsService settingsService,
    SecurityService securityService) {
    this.appProperties = appProperties;
    this.cryptoSymmetricService = cryptoSymmetricService;
    this.srs = srs;
    this.settingsService = settingsService;
    this.securityService = securityService;
  }

  private void createAESKey(String settingName, boolean encryptValue)
  throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException,
         NoSuchPaddingException, IOException {

    final byte[] aes = cryptoSymmetricService.generateKey(
      appProperties.getSecuritySymmetricKeySize(),
      appProperties.getSecuritySymmetricKeyAlgorithm()).getEncoded();
    log.log(Level.FINEST, "Generating AES key: {0}.", Arrays.toString(aes));
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
    if (!srs.exists(Security.AES_KEY)) {
      log.log(Level.CONFIG, "Platform AES key is not set, creating one now.");
      try {
        createAESKey(Security.AES_KEY, false);
      } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
        NoSuchPaddingException | IOException ex) {
        log.log(Level.SEVERE, "Could not create an AES key for the platform, terminating.", ex);
        System.exit(ExitCodes.CANT_GENERATE_PLATFORM_AES_KEY);
      }
    }
  }

  /**
   * Generate platform's provisioning key if none is already generated.
   */
  private void generateProvisioningAESKey() {
    if (!srs.exists(Provisioning.AES_KEY)) {
      try {
        createAESKey(Provisioning.AES_KEY, true);
      } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
        NoSuchPaddingException | IOException ex) {
        log.log(Level.SEVERE, "Could not create an AES key for provisioning, terminating.", ex);
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
  }
}
