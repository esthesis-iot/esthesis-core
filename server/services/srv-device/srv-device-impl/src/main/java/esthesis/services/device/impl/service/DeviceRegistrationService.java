package esthesis.services.device.impl.service;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.DeviceRegistrationMode;
import esthesis.common.AppConstants.DeviceStatus;
import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.exception.QDisabledException;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QSecurityException;
import esthesis.service.audit.resource.AuditResource;
import esthesis.service.crypto.dto.CreateCertificateRequestDTO;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.device.dto.DeviceKeyDTO;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.device.impl.repository.DeviceRepository;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
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
  @RestClient
  AuditResource auditResource;

  @Inject
  JsonWebToken jwt;

  /**
   * Checks if device-pushed tags exist in the system and report the ones that do not exist.
   *
   * @param tags the list of tag names to check.
   */
  private void checkTags(String hardwareId, List<String> tags) {
    for (String tag : tags) {
      if (tagResource.findByName(tag, false) == null) {
        log.warn("Device-pushed tag '{}' for device with hardware id '{}' does not exist.", tag,
            hardwareId);
      }
    }
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
    String ids = deviceRegistration.getIds();
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
      register(hardwareId, deviceRegistration.getTags(), DeviceStatus.PREREGISTERED);
//      auditResource.save(
//          new AuditEntity()
//              .setCreatedOn(Instant.now())
//              .setCreatedBy(jwt.getName())
//              .setMessage("Preregistering device with hardware id '" + hardwareId + "'.")
//              .setOperation(Operation.WRITE)
//              .setCategory(AppConstants.Audit.Category.DEVICE));
    }
  }

  public DeviceEntity register(DeviceRegistrationDTO deviceRegistration)
  throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, OperatorCreationException,
         NoSuchProviderException {
    log.debug("Attempting to register device with hardwareId ''{}'' and tags ''{}''.",
        deviceRegistration.getIds(), deviceRegistration.getTags());

    DeviceRegistrationMode deviceRegistrationMode = DeviceRegistrationMode.valueOf(
        settingsResource.findByName(NamedSetting.DEVICE_REGISTRATION_MODE).asString());

    if (deviceRegistrationMode == DeviceRegistrationMode.DISABLED) {
      throw new QDisabledException("Registration of new devices is disabled.",
          deviceRegistration.getIds());
    } else {
      // Check registration preconditions and register device.
      log.debug("Platform running on '{}' registration mode.", deviceRegistrationMode);
      return switch (deviceRegistrationMode) {
        case OPEN -> register(deviceRegistration.getIds(), deviceRegistration.getTags(),
            DeviceStatus.REGISTERED);
        case OPEN_WITH_APPROVAL ->
            register(deviceRegistration.getIds(), deviceRegistration.getTags(),
                DeviceStatus.APPROVAL);
        case ID -> activatePreregisteredDevice(deviceRegistration.getIds());
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
      AppConstants.DeviceStatus status)
  throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, OperatorCreationException,
         NoSuchProviderException {
    log.debug("Registering device with hardware id '{}'.", hardwareId);

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
        .setCreatedOn(Instant.now())
        .setDeviceKey(deviceKeyDTO);
    if (status != DeviceStatus.PREREGISTERED) {
      deviceEntity.setRegisteredOn(Instant.now());
    }

    // Set device-pushed tags by converting the tag names to tag ids.
    if (!tags.isEmpty()) {
      checkTags(hardwareId, tags);
      deviceEntity.setTags(
          tags.stream().map(tag -> tagResource.findByName(tag, false).getId().toString())
              .collect(Collectors.toList()));
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
