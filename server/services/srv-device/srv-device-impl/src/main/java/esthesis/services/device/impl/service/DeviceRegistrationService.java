package esthesis.services.device.impl.service;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.DeviceRegistrationMode;
import esthesis.common.AppConstants.DeviceStatus;
import esthesis.common.AppConstants.DeviceType;
import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.exception.QDisabledException;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QSecurityException;
import esthesis.service.crypto.dto.CreateCertificateRequestDTO;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.device.dto.DeviceKeyDTO;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.device.impl.repository.DeviceRepository;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DeviceRegistrationService {

  @Inject
  DeviceRepository deviceRepository;

  @Inject
  @RestClient
  KeyResource keyResource;

  @Inject
  @RestClient
  TagResource tagResource;

  @Inject
  @RestClient
  SettingsResource settingsResource;

  @Inject
  JsonWebToken jwt;

  /**
   * Checks if device-pushed tags exist in the system and report the ones that do not exist,
   * optionally creating missing tag.s
   *
   * @param hardwareId    The hardware id of the device sending the tags.
   * @param tags          The list of tag names to check.
   * @param createMissing Whether to create missing tags or not.
   * @return Returns the list of tags that can be assigned to the device, comprising of either
   * existing tags or newly created tags.
   */
  private List<String> checkTags(String hardwareId, List<String> tags, boolean createMissing) {
    List<String> validTags = new ArrayList<>();

    for (String tag : tags) {
      if (tagResource.findByName(tag, false) == null) {
        log.warn("Device-pushed tag '{}' for device with hardware id '{}' does not exist.", tag,
            hardwareId);
        if (createMissing) {
          log.debug("Creating missing tag '{}' for device with hardware id '{}'.", tag, hardwareId);
          TagEntity tagEntity = new TagEntity();
          tagEntity.setName(tag);
          tagResource.save(tagEntity);
          validTags.add(tag);
        }
      } else {
        validTags.add(tag);
      }
    }

    return validTags;
  }

  /**
   * Preregisters a device, so that it can self-register later on.
   *
   * @param deviceRegistration The preregistration details of the device.
   */
  public void preregister(DeviceRegistrationDTO deviceRegistration)
  throws NoSuchAlgorithmException, OperatorCreationException, InvalidKeySpecException,
         NoSuchProviderException, IOException {
    // Split IDs.
    //TODO ********************************************* CHECK HOW TO SPLIT
    String ids = deviceRegistration.getHardwareId();
    ids = ids.replace("\n", ",");
    String[] idList = ids.split(",");

    // Before preregistering the devices check that all given registration IDs
    // are available. If any of the given IDs is already assigned on an
    // existing device abort the preregistration.
    for (String hardwareId : idList) {
      if (deviceRepository.findByHardwareId(hardwareId).isPresent()) {
        throw new QAlreadyExistsException("Preregistration id '{}' is already assigned to a device "
            + "registered in the system.", hardwareId);
      }
    }

    // Register IDs.
    for (String hardwareId : idList) {
      log.debug("Requested to preregister a device with hardware id '{}'.", hardwareId);
      register(hardwareId, deviceRegistration.getTags(), DeviceStatus.PREREGISTERED,
          deviceRegistration.getType());
    }
  }

  public DeviceEntity register(DeviceRegistrationDTO deviceRegistration)
  throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, OperatorCreationException,
         NoSuchProviderException {
    log.debug("Attempting to register device with '{}'.", deviceRegistration);

    DeviceRegistrationMode deviceRegistrationMode = DeviceRegistrationMode.valueOf(
        settingsResource.findByName(NamedSetting.DEVICE_REGISTRATION_MODE).asString());

    if (deviceRegistrationMode == DeviceRegistrationMode.DISABLED) {
      throw new QDisabledException("Registration of new devices is disabled.",
          deviceRegistration.getHardwareId());
    } else {
      // Check registration preconditions and register device.
      log.debug("Platform running in '{}' registration mode.", deviceRegistrationMode);
      return switch (deviceRegistrationMode) {
        case OPEN -> register(deviceRegistration.getHardwareId(), deviceRegistration.getTags(),
            DeviceStatus.REGISTERED, deviceRegistration.getType());
        case OPEN_WITH_APPROVAL ->
            register(deviceRegistration.getHardwareId(), deviceRegistration.getTags(),
                DeviceStatus.APPROVAL, deviceRegistration.getType());
        case ID -> activatePreregisteredDevice(deviceRegistration.getHardwareId());
        default ->
            throw new QDoesNotExistException("The requested registration mode does not exist.");
      };
    }
  }

  /**
   * The internal registration handler.
   *
   * @param hardwareId The hardware id of the device to be registered.
   * @param tags       The tag names associated with this device as a comma-separated list.
   */
  private DeviceEntity register(String hardwareId, List<String> tags,
      AppConstants.DeviceStatus status, DeviceType deviceType)
  throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, OperatorCreationException,
         NoSuchProviderException {
    log.debug("Registering device with hardware id '{}'.", hardwareId);

    // Check the proposed hardware id conforms to the naming convention.
    if (!hardwareId.matches(AppConstants.HARDWARE_ID_REGEX)) {
      throw new QMismatchException(
          "Hardware id '{}' does not conform to the naming convention '{}'.", hardwareId,
          AppConstants.HARDWARE_ID_REGEX);
    }

    // Check that a device with the same hardware ID does not already exist.
    if (deviceRepository.findByHardwareId(hardwareId).isPresent()) {
      throw new QAlreadyExistsException(
          "A device with hardware id '{}' is already registered with the platform.", hardwareId);
    }

    // Create a keypair for the device to be registered.
    KeyPair keyPair = keyResource.generateKeyPair();

    // Set the security keys for the new device.
    final DeviceKeyDTO deviceKeyDTO = new DeviceKeyDTO()
        .setPublicKey(keyResource.publicKeyToPEM(keyPair.getPublic()))
        .setPrivateKey(keyResource.privateKeyToPEM(keyPair.getPrivate())).setRolledOn(Instant.now())
        .setRolledOn(Instant.now())
        .setRolledAccepted(true);

    // Create a certificate for this device if the root CA is set.
    SettingEntity deviceRootCA = settingsResource.findByName(NamedSetting.DEVICE_ROOT_CA);
    if (deviceRootCA != null) {
      deviceKeyDTO.setCertificate(keyResource.generateCertificateAsPEM(
          new CreateCertificateRequestDTO().setCn(hardwareId).setKeyPair(keyPair)));
      deviceKeyDTO.setCertificateCaId(
          settingsResource.findByName(NamedSetting.DEVICE_ROOT_CA).asString());
    } else {
      log.warn("No root CA is set to create a device certificates for device with hardware id "
          + "'{}'.", hardwareId);
    }

    // Create the new device.
    final DeviceEntity deviceEntity = new DeviceEntity()
        .setHardwareId(hardwareId)
        .setStatus(status)
        .setType(deviceType)
        .setCreatedOn(Instant.now())

        .setDeviceKey(deviceKeyDTO);
    if (status != DeviceStatus.PREREGISTERED) {
      deviceEntity.setRegisteredOn(Instant.now());
    }

    // Set device-pushed tags by converting the tag names to tag ids.
    if (!CollectionUtils.isEmpty(tags)) {
      List<String> validTags = checkTags(hardwareId, tags,
          settingsResource.findByName(NamedSetting.DEVICE_PUSHED_TAGS).asBoolean());
      deviceEntity.setTags(
          validTags.stream().map(tag -> tagResource.findByName(tag, false).getId().toString())
              .toList());
    }

    deviceRepository.persist(deviceEntity);

    return deviceEntity;
  }

  /**
   * Activate a preregistered device. There is no actual device registration taking place here as
   * the device already exists in system's database.
   *
   * @param hardwareId The hardware id of the device to activate.
   */
  public DeviceEntity activatePreregisteredDevice(String hardwareId) {
    Optional<DeviceEntity> optionalDevice = deviceRepository.findByHardwareId(hardwareId);

    // Check that a device with the same hardware ID is not already registered.
    if (optionalDevice.isPresent() && !optionalDevice.get().getStatus()
        .equals(DeviceStatus.PREREGISTERED)) {
      throw new QSecurityException(
          "Cannot register device with hardwareId {} as it is already in {} state.", hardwareId,
          optionalDevice.get().getStatus());
    } else if (!optionalDevice.isPresent()) {
      throw new QSecurityException("Device with hardware ID {} does not exist.", hardwareId);
    }

    // Find the device and set its status to registered.
    DeviceEntity deviceEntity = optionalDevice.get();
    deviceEntity.setStatus(DeviceStatus.REGISTERED);
    deviceEntity.setRegisteredOn(Instant.now());
    deviceRepository.update(deviceEntity);

    return deviceEntity;
  }
}
