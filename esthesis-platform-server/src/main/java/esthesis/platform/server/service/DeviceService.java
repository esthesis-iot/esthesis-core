package esthesis.platform.server.service;


import static esthesis.platform.server.config.AppSettings.SettingValues.DeviceRegistration.RegistrationMode.DISABLED;

import com.eurodyn.qlack.common.exception.QAlreadyExistsException;
import com.eurodyn.qlack.common.exception.QDisabledException;
import com.eurodyn.qlack.common.exception.QSecurityException;
import com.eurodyn.qlack.fuse.crypto.CryptoAsymmetricService;
import com.eurodyn.qlack.fuse.crypto.CryptoSymmetricService;
import com.eurodyn.qlack.fuse.crypto.dto.CreateKeyPairDTO;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.google.common.collect.Lists;
import esthesis.extension.device.request.RegistrationRequest;
import esthesis.platform.server.config.AppConstants.Device.Status;
import esthesis.platform.server.config.AppConstants.WebSocket.Topic;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.config.AppSettings.Setting.DeviceRegistration;
import esthesis.platform.server.config.AppSettings.SettingValues.DeviceRegistration.PushTags;
import esthesis.platform.server.config.AppSettings.SettingValues.DeviceRegistration.RegistrationMode;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.DeviceKeyDTO;
import esthesis.platform.server.dto.DeviceRegistrationDTO;
import esthesis.platform.server.dto.WebSocketMessageDTO;
import esthesis.platform.server.mapper.DeviceKeyMapper;
import esthesis.platform.server.mapper.DeviceMapper;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.model.DeviceKey;
import esthesis.platform.server.repository.DeviceKeyRepository;
import esthesis.platform.server.repository.DeviceRepository;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class DeviceService extends BaseService<DeviceDTO, Device> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DeviceService.class.getName());

  private final DeviceRepository deviceRepository;
  private final DeviceMapper deviceMapper;
  private final DeviceKeyMapper deviceKeyMapper;
  private final WebSocketService webSocketService;
  private final SettingResolverService srs;
  private final TagService tagService;
  private final DeviceKeyRepository deviceKeyRepository;
  private final SecurityService securityService;
  private final AppProperties appProperties;
  private final CryptoAsymmetricService cryptoAsymmetricService;
  private final CryptoSymmetricService cryptoSymmetricService;
  private final CertificatesService certificatesService;

  public DeviceService(
    DeviceRepository deviceRepository, DeviceMapper deviceMapper,
    DeviceKeyMapper deviceKeyMapper,
    WebSocketService webSocketService, SettingResolverService srs,
    TagService tagService,
    DeviceKeyRepository deviceKeyRepository,
    SecurityService securityService, AppProperties appProperties,
    CryptoAsymmetricService cryptoAsymmetricService,
    CryptoSymmetricService cryptoSymmetricService,
    CertificatesService certificatesService) {
    this.deviceRepository = deviceRepository;
    this.deviceMapper = deviceMapper;
    this.deviceKeyMapper = deviceKeyMapper;
    this.webSocketService = webSocketService;
    this.srs = srs;
    this.tagService = tagService;
    this.deviceKeyRepository = deviceKeyRepository;
    this.securityService = securityService;
    this.appProperties = appProperties;
    this.cryptoAsymmetricService = cryptoAsymmetricService;
    this.cryptoSymmetricService = cryptoSymmetricService;
    this.certificatesService = certificatesService;
  }

  /**
   * Preregister a device, so that it can self-register later on.
   */
  @Async
  public void preregister(DeviceRegistrationDTO deviceRegistrationDTO)
  throws NoSuchAlgorithmException, IOException, InvalidKeyException,
         BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
         NoSuchPaddingException {
    // Split IDs.
    String ids = deviceRegistrationDTO.getIds();
    ids = ids.replace("\n", ",");
    String[] idList = ids.split(",");

    // Before preregistering the devices check that all given registration IDs are available. If any
    // of the given IDs is already assigned on an existing device abort the preregistration.
    for (String hardwareId : idList) {
      if (deviceRepository.findByHardwareId(hardwareId).isPresent()) {
        throw new QAlreadyExistsException("Preregistration ID {0} is already assigned to a device "
          + "registered in the system.", hardwareId);
      }
    }
    // Convert tags to their name-equivalent.
    String tagNames = String.join(",", deviceRegistrationDTO.getTags().stream().map(tagId -> {
      return tagService.findById(tagId).getName();
    }).collect(Collectors.toList()));

    // Register IDs.
    for (String hardwareId : idList) {
      register(hardwareId, tagNames, Status.PREREGISTERED, false);
    }
  }

  private void checkTags(String tags) {
    if (StringUtils.isNotBlank(tags)) {
      if (srs.is(DeviceRegistration.PUSH_TAGS, PushTags.ALLOWED)) {
        for (String tag : tags.split(",")) {
          tag = tag.trim();
          if (!tagService.findByName(tag).isPresent()) {
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
  }

  private void registerPreregistered(RegistrationRequest registrationRequest, String hardwareId) {
    Optional<Device> optionalDevice = deviceRepository.findByHardwareId(hardwareId);

    // Check that a device with the same hardware ID is not already registered.
    if (optionalDevice.isPresent() && !optionalDevice.get().getState()
      .equals(Status.PREREGISTERED)) {
      throw new QSecurityException(
        "Cannot register device with hardware ID {0} as it is already in {1} state.",
        hardwareId, optionalDevice.get().getState());
    } else if (!optionalDevice.isPresent()) {
      throw new QSecurityException(
        "Device with hardware ID {0} does not exist.", hardwareId);
    }
    Device device = optionalDevice.get();

    // Check tags and display appropriate warnings if necessary.
    checkTags(registrationRequest.getTags());

    // Add device-pushed tags if supported.
    if (srs.is(DeviceRegistration.PUSH_TAGS, PushTags.ALLOWED) && StringUtils
      .isNotBlank(registrationRequest.getTags())) {
      device.setTags(
        Lists.newArrayList(
          tagService.findAllByNameIn(Arrays.asList(registrationRequest.getTags().split(",")))));
    }

    // Set the status to registered.
    device.setState(Status.REGISTERED);

    deviceRepository.save(device);
  }

  private void register(String hardwareId, String tags, String state, boolean checkTags)
  throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException,
         InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException,
         NoSuchPaddingException {
    // Create a keypair.
    CreateKeyPairDTO createKeyPairDTO = new CreateKeyPairDTO();
    createKeyPairDTO.setKeySize(appProperties.getSecurityAsymmetricKeySize());
    createKeyPairDTO
      .setKeyPairGeneratorAlgorithm(appProperties.getSecurityAsymmetricKeyAlgorithm());
    final KeyPair keyPair = cryptoAsymmetricService.createKeyPair(createKeyPairDTO);

    // Check that a device with the same hardware ID does not already exist.
    if (deviceRepository.findByHardwareId(hardwareId).isPresent()) {
      throw new QAlreadyExistsException(
        "Device with hardware ID {0} is already registered with the platform.", hardwareId);
    }

    // Check tags and display appropriate warnings if necessary.
    if (checkTags) {
      checkTags(tags);
    }

    // Create the new device.
    final Device device = new Device()
      .setHardwareId(hardwareId)
      .setState(state);
    deviceRepository.save(device);

    // Create the keys for the new device.
    final DeviceKey deviceKey = new DeviceKey()
      .setPrivateKey(securityService.encrypt(cryptoAsymmetricService.privateKeyToPEM(keyPair)))
      .setPublicKey(cryptoAsymmetricService.publicKeyToPEM(keyPair))
      .setPsPublicKey(certificatesService.getPSPublicKey())
      .setSessionKey(securityService.encrypt(
        cryptoSymmetricService.generateKey(
          appProperties.getSecuritySymmetricKeySize(),
          appProperties.getSecuritySymmetricKeyAlgorithm())))
      .setRolledOn(Instant.now())
      .setRolledAccepted(true)
      .setDevice(device);
    deviceKeyRepository.save(deviceKey);

    // Add device-pushed tags if supported.
    if (srs.is(DeviceRegistration.PUSH_TAGS, PushTags.ALLOWED)) {
      device
        .setTags(Lists.newArrayList(tagService.findAllByNameIn(Arrays.asList(tags.split(",")))));
    }
  }

  public DeviceDTO register(RegistrationRequest registrationRequest, String hardwareId)
  throws NoSuchAlgorithmException, IOException, InvalidKeyException,
         NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException,
         IllegalBlockSizeException {
    DeviceDTO deviceDTO = null;

    if (srs.is(DeviceRegistration.REGISTRATION_MODE, DISABLED)) {
      throw new QDisabledException(
        "Attempting to register device with hardware ID {0} but registration of new devices is "
          + "disabled.", hardwareId);
    } else {
      LOGGER.log(Level.FINE, "Attempting to register device with registration ID {0}.",
        hardwareId);
      // Check registration preconditions and register device.
      LOGGER.log(Level.FINEST, "Platform running on {0} registration mode.",
        srs.get(DeviceRegistration.REGISTRATION_MODE));
      switch (srs.get(DeviceRegistration.REGISTRATION_MODE)) {
        case RegistrationMode.OPEN:
          register(hardwareId, registrationRequest.getTags(), Status.REGISTERED, true);
          deviceDTO = findByHardwareId(hardwareId);
          break;
        case RegistrationMode.OPEN_WITH_APPROVAL:
          register(hardwareId,
            registrationRequest.getTags(), Status.APPROVAL, true);
          deviceDTO = findByHardwareId(hardwareId);
          break;
        case RegistrationMode.ID:
          registerPreregistered(registrationRequest, hardwareId);
          deviceDTO = findByHardwareId(hardwareId);
          break;
        case RegistrationMode.DISABLED:
          throw new QDisabledException("Device registration is disabled.");
      }

      // Realtime notification.
      webSocketService.publish(new WebSocketMessageDTO()
        .setTopic(Topic.DEVICE_REGISTRATION)
        .setPayload(
          MessageFormat.format("Device with registration id {0} registered.", hardwareId)));
    }

    LOGGER.log(Level.FINE, "Registered device with hardware ID {0}.", hardwareId);
    return deviceDTO;
  }

  private DeviceDTO findKeys(DeviceDTO deviceDTO) {
    final DeviceKey keys = deviceKeyRepository.findLatestAccepted(deviceDTO.getId());
    try {
      deviceDTO.setPublicKey(keys.getPublicKey());
      deviceDTO.setPrivateKey(new String(securityService.decrypt(keys.getPrivateKey()),
        StandardCharsets.UTF_8));
      deviceDTO
        .setSessionKey(Base64.encodeBase64String(securityService.decrypt(keys.getSessionKey())));
    } catch (NoSuchPaddingException | InvalidKeyException | BadPaddingException |
      IllegalBlockSizeException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
      LOGGER.log(Level.SEVERE, "Could not obtain device's cryptographic keys.", e);
    }

    return deviceDTO;
  }

  public DeviceKeyDTO findKeys(long deviceId) {
    return deviceKeyMapper.map(deviceKeyRepository.findLatestAccepted(deviceId));
  }

  public DeviceDTO findByHardwareId(String hardwareId) {
    final DeviceDTO deviceDTO = deviceMapper
      .map(ReturnOptional.r(deviceRepository.findByHardwareId(hardwareId), hardwareId));
    findKeys(deviceDTO);

    return deviceDTO;
  }

  @Override
  public DeviceDTO findById(long id) {
    final DeviceDTO deviceDTO = super.findById(id);
    findKeys(deviceDTO);

    return deviceDTO;
  }

  @Async
  @Override
  public DeviceDTO deleteById(long id) {
    return super.deleteById(id);
  }
}
