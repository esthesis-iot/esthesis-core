package esthesis.platform.server.util;

import static esthesis.platform.server.config.AppConstants.Device.RegistrationMode.DISABLED;
import static esthesis.platform.server.config.AppConstants.Device.Status.APPROVAL;
import static esthesis.platform.server.config.AppConstants.Device.Status.REGISTERED;

import com.eurodyn.qlack.common.exception.QAlreadyExistsException;
import com.eurodyn.qlack.common.exception.QDisabledException;
import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QMismatchException;
import com.eurodyn.qlack.fuse.crypto.CryptoConversionService;
import com.eurodyn.qlack.fuse.crypto.CryptoDigestService;
import com.eurodyn.qlack.fuse.crypto.CryptoKeyService;
import com.eurodyn.qlack.fuse.crypto.dto.CreateKeyPairDTO;
import com.eurodyn.qlack.fuse.settings.service.SettingsService;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.google.common.collect.Lists;
import esthesis.platform.common.config.AppConstants.Generic;
import esthesis.platform.server.config.AppConstants.Setting;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
import esthesis.platform.server.repository.TagRepository;
import esthesis.platform.server.service.SecurityService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility class to provide different types of checks needed during registration of devices under the supported
 * registration modes as well as registration functions.
 */
@Component
public class RegistrationUtil {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(RegistrationUtil.class.getName());

  private final DeviceRepository deviceRepository;
  private final CryptoDigestService cryptoDigestService;
  private final CryptoKeyService cryptoKeyService;
  private final AppProperties appProperties;
  private final CryptoConversionService cryptoConversionService;
  private final SecurityService securityService;
  private final TagRepository tagRepository;
  private final SettingsService settingsService;

  public RegistrationUtil(DeviceRepository deviceRepository, CryptoDigestService cryptoDigestService,
      CryptoKeyService cryptoKeyService, AppProperties appProperties,
      CryptoConversionService cryptoConversionService, SecurityService securityService,
      TagRepository tagRepository, SettingsService settingsService) {
    this.deviceRepository = deviceRepository;
    this.cryptoDigestService = cryptoDigestService;
    this.cryptoKeyService = cryptoKeyService;
    this.appProperties = appProperties;
    this.cryptoConversionService = cryptoConversionService;
    this.securityService = securityService;
    this.tagRepository = tagRepository;
    this.settingsService = settingsService;
  }

  public void checkRegistrationEnabled() {
    if (settingsService.getSetting(Generic.SYSTEM, Setting.DEVICE_REGISTRATION, Generic.SYSTEM).getVal()
        .equals(DISABLED)) {
      throw new QDisabledException("Device registration is disabled.");
    }
  }

  public void checkDeviceIdDoesNotExist(String deviceId) {
    if (deviceRepository.findByDeviceId(deviceId).isPresent()) {
      throw new QAlreadyExistsException(MessageFormat.format("Device with id {0} already exists.", deviceId));
    }
  }

  public void checkDeviceIdPreregistered(String deviceId) {
    Device device = ReturnOptional.r(deviceRepository.findByDeviceId(deviceId));
    if (!device.getState().equals(REGISTERED)) {
      throw new QDoesNotExistException("A device with id {0} is not preregistered.", deviceId);
    }
  }

  public void checkHmac(String deviceId, String hmac) throws InvalidKeyException, NoSuchAlgorithmException {
    LOGGER.log(Level.FINEST, "Received HMAC: {0}.", hmac);
    Device device = ReturnOptional.r(deviceRepository.findByDeviceId(deviceId));
    String calculatedHmac = cryptoDigestService
        .hmacSha256(securityService.decrypt(device.getPrivateKey()), device.getPublicKey());
    LOGGER.log(Level.FINEST, "Calculated HMAC: {0}.", calculatedHmac);
    if (!calculatedHmac.equals(hmac)) {
      throw new QMismatchException("HMAC {0} can not be verified.", hmac);
    }
  }

  public void checkTagsExist(String tags) {
    for (String tag : tags.split(",")) {
      tag = tag.trim();
      if (!tagRepository.findByName(tag).isPresent()) {
        throw new QDoesNotExistException("Tag {0} does not exist.", tag);
      }
    }
  }

  private KeyPair createKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
    return cryptoKeyService
        .createKeyPair(CreateKeyPairDTO.builder().keySize(appProperties.getSecurityCaKeypairKeySize())
            .generatorAlgorithm(appProperties.getSecurityCaKeypairGeneratorAlgorithm())
            .generatorProvider(appProperties.getSecurityCaKeypairGeneratorProvider())
            .secretAlgorithm(appProperties.getSecurityCaKeypairSecrectAlgorithm())
            .secretProvider(appProperties.getSecurityCaKeypairSecrectProvider())
            .build());
  }

  public Device registerNew(String deviceId, String tags)
      throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
    final KeyPair keyPair = createKeyPair();

    return deviceRepository.save(new Device()
        .setDeviceId(deviceId)
        .setPublicKey(cryptoConversionService.publicKeyToPEM(keyPair))
        .setPrivateKey(securityService.encrypt(cryptoConversionService.privateKeyToPEM(keyPair)))
        .setState(REGISTERED)
        .setTags(Lists.newArrayList(tagRepository.findAllByNameIn(Arrays.asList(tags.split(","))))));
  }

  public Device registerForApproval(String deviceId, String tags)
      throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
    final KeyPair keyPair = createKeyPair();

    return deviceRepository.save(new Device()
        .setDeviceId(deviceId)
        .setPublicKey(cryptoConversionService.publicKeyToPEM(keyPair))
        .setPrivateKey(securityService.encrypt(cryptoConversionService.privateKeyToPEM(keyPair)))
        .setState(APPROVAL)
        .setTags(Lists.newArrayList(tagRepository.findAllByNameIn(Arrays.asList(tags.split(","))))));
  }

  public Device registerPreregistered(String deviceId, String tags) {
    Device device = ReturnOptional.r(deviceRepository.findByDeviceId(deviceId));
    device.setState(REGISTERED);

    return deviceRepository.save(device);
  }

  public Device registerCrypto(String deviceId, String tags) {
    Device device = ReturnOptional.r(deviceRepository.findByDeviceId(deviceId));
    device.setState(REGISTERED);

    return deviceRepository.save(device);
  }

}
