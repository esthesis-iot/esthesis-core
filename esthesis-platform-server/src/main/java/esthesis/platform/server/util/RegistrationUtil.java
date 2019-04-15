package esthesis.platform.server.util;

import com.eurodyn.qlack.common.exception.QAlreadyExistsException;
import com.eurodyn.qlack.fuse.crypto.CryptoConversionService;
import com.eurodyn.qlack.fuse.crypto.CryptoKeyService;
import com.eurodyn.qlack.fuse.crypto.dto.CreateKeyPairDTO;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.google.common.collect.Lists;
import esthesis.extension.device.request.RegistrationRequest;
import esthesis.platform.server.config.AppConstants.Device.Status;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.config.AppSettings.Setting.DeviceRegistration;
import esthesis.platform.server.config.AppSettings.SettingValues.DeviceRegistration.PushTags;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
import esthesis.platform.server.repository.TagRepository;
import esthesis.platform.server.service.SecurityService;
import esthesis.platform.server.service.SettingResolverService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility class to provide different types of checks needed during registration of devices.
 */
@Component
public class RegistrationUtil {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(RegistrationUtil.class.getName());

  private final DeviceRepository deviceRepository;
  private final CryptoKeyService cryptoKeyService;
  private final AppProperties appProperties;
  private final CryptoConversionService cryptoConversionService;
  private final SecurityService securityService;
  private final TagRepository tagRepository;
  private final SettingResolverService srs;

  public RegistrationUtil(DeviceRepository deviceRepository,
    CryptoKeyService cryptoKeyService, AppProperties appProperties,
    CryptoConversionService cryptoConversionService, SecurityService securityService,
    TagRepository tagRepository, SettingResolverService srs) {
    this.deviceRepository = deviceRepository;
    this.cryptoKeyService = cryptoKeyService;
    this.appProperties = appProperties;
    this.cryptoConversionService = cryptoConversionService;
    this.securityService = securityService;
    this.tagRepository = tagRepository;
    this.srs = srs;
  }

  private void checkTags(String tags) {
    if (srs.is(DeviceRegistration.PUSH_TAGS, PushTags.ALLOWED)) {
      for (String tag : tags.split(",")) {
        tag = tag.trim();
        if (!tagRepository.findByName(tag).isPresent()) {
          LOGGER
            .log(Level.WARNING, "Device-pushed tag {0} does not exist and will be ignored.", tag);
        }
      }
    } else {
      if (StringUtils.isNotBlank(tags)) {
        LOGGER.log(Level.FINE, "Device-pushed tags {0} will be ignored.", tags);
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

  public Device register(String hardwareId, String tags, String state, boolean checkTags)
    throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
    final KeyPair keyPair = createKeyPair();

    // Check that a device with the same hardware ID does not already exist.
    if (deviceRepository.findByHardwareId(hardwareId)
      .isPresent()) {
      throw new QAlreadyExistsException(
        "Device with hardware ID {0} is already registered with the platform.", hardwareId);
    }

    // Check tags and display appropriate warnings if necessary.
    if (checkTags) {
      checkTags(tags);
    }

    // Prepare the new device.
    final Device device = new Device()
      .setPublicKey(cryptoConversionService.publicKeyToPEM(keyPair))
      .setPrivateKey(securityService.encrypt(cryptoConversionService.privateKeyToPEM(keyPair)))
      .setHardwareId(hardwareId)
      .setState(state);

    // Add device-pushed tags if supported.
    if (srs.is(DeviceRegistration.PUSH_TAGS, PushTags.ALLOWED)) {
      device.setTags(
        Lists.newArrayList(
          tagRepository.findAllByNameIn(Arrays.asList(tags.split(",")))));
    }

    return deviceRepository.save(device);
  }


  public Device registerPreregistered(RegistrationRequest registrationRequest) {
    Device device = ReturnOptional
      .r(deviceRepository.findByHardwareId(registrationRequest.getHardwareId()),
        registrationRequest.getHardwareId());

    // Check tags and display appropriate warnings if necessary.
    checkTags(registrationRequest.getTags());

    // Add device-pushed tags if supported.
    if (srs.is(DeviceRegistration.PUSH_TAGS, PushTags.ALLOWED)) {
      device.setTags(
        Lists.newArrayList(
          tagRepository.findAllByNameIn(Arrays.asList(registrationRequest.getTags().split(",")))));
    }

    // Set the status to registered.
    device.setState(Status.REGISTERED);

    return deviceRepository.save(device);
  }

}
