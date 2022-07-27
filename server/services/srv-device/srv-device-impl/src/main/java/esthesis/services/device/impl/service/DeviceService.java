package esthesis.services.device.impl.service;


import esthesis.common.AppConstants.Device.State;
import esthesis.common.AppConstants.Registry;
import esthesis.common.dto.DeviceMessage;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.service.BaseService;
import esthesis.service.crypto.dto.CertificateRequest;
import esthesis.service.crypto.dto.KeyPairResponse;
import esthesis.service.crypto.resource.CryptoResourceV1;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DeviceKey;
import esthesis.service.device.dto.DevicePage;
import esthesis.service.device.dto.DeviceRegistration;
import esthesis.service.device.dto.RegistrationRequest;
import esthesis.service.registry.dto.RegistryEntry;
import esthesis.service.registry.resource.RegistryResourceV1;
import esthesis.service.tag.resource.TagResourceV1;
import esthesis.services.device.impl.repository.DeviceRepository;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DeviceService extends BaseService<Device> {

  @Inject
  JsonWebToken jwt;

  @Inject
  DeviceRepository deviceRepository;

  @Inject
  @RestClient
  CryptoResourceV1 cryptoResourceV1;

  @Inject
  @RestClient
  TagResourceV1 tagResourceV1;

  @Inject
  @RestClient
  RegistryResourceV1 registryResourceV1;

  /**
   * Populates the cryptographic keys of a device.
   *
   * @param Device The DTO of the device to populate the keys into.
   */
  private Device fillCryptoKeys(Device Device) {
//    final DeviceKey keys = deviceKeyRepository.findLatestAccepted(
//        Device.getId());
//    Device.setPrivateKey(keys.getPrivateKey());
//    Device.setPublicKey(keys.getPublicKey());
//    Device.setCertificate(keys.getCertificate());
//
//    return Device;
    return null;
  }

  /**
   * Check if device-pushed tags exist in the system and report the ones that do
   * not exist.
   *
   * @param tags the list of tags to check.
   */
  private void checkTags(List<String> tags) {
    for (String tag : tags) {
      if (tagResourceV1.findByName(tag) == null) {
        log.warn("Device-pushed tag '{}' does not exist.", tag);
      }
    }
  }

  /**
   * Activate a preregistered device. There is no actual device registration
   * taking place here as the device already exists in system's database.
   *
   * @param hardwareId The hardware Id of the device to activate.
   */
  private void activatePreregisteredDevice(String hardwareId) {
//    Optional<Device> optionalDevice = deviceRepository.findByHardwareId(
//        hardwareId);
//
//    // Check that a device with the same hardware ID is not already registered.
//    if (optionalDevice.isPresent() && !optionalDevice.get().getState()
//        .equals(State.PREREGISTERED)) {
//      throw new QSecurityException(
//          "Cannot register device with hardware ID {0} as it is already in {1} state.",
//          hardwareId, optionalDevice.get().getState());
//    } else if (!optionalDevice.isPresent()) {
//      throw new QSecurityException(
//          "Device with hardware ID {0} does not exist.", hardwareId);
//    }
//
//    // Find the device and set its status to registered.
//    Device device = optionalDevice.get();
//    device.setState(State.REGISTERED);
//    deviceRepository.save(device);
  }

  /**
   * Preregisters a device, so that it can self-register later on.
   *
   * @param deviceRegistration The preregistration details of the device.
   */
  @Transactional
  public void preregister(DeviceRegistration deviceRegistration)
  throws NoSuchAlgorithmException, OperatorCreationException,
         InvalidKeySpecException,
         NoSuchProviderException, IOException {
    // Split IDs.
    String ids = deviceRegistration.getIds();
    ids = ids.replace("\n", ",");
    String[] idList = ids.split(",");

    // Before preregistering the devices check that all given registration IDs are available. If any
    // of the given IDs is already assigned on an existing device abort the preregistration.
    for (String hardwareId : idList) {
      if (deviceRepository.findByHardwareId(hardwareId).isPresent()) {
        throw new QAlreadyExistsException(
            "Preregistration id '{0}' is already assigned to a device "
                + "registered in the system.", hardwareId);
      }
    }

    // Register IDs.
    for (String hardwareId : idList) {
      register(hardwareId, deviceRegistration.getTags(), State.PREREGISTERED);
    }
  }

  /**
   * The internal registration handler, see
   * {@link #register(DeviceMessage, String)}.
   *
   * @param hardwareId The hardware Id of the device to be registered.
   * @param tags       The tags associated with this device as a comma-separated
   *                   list.
   * @param state      The initial state of the registration of the device.
   */
  private void register(String hardwareId, List<String> tags, String state)
  throws IOException, InvalidKeySpecException, NoSuchAlgorithmException,
         OperatorCreationException, NoSuchProviderException {
    // Check that a device with the same hardware ID does not already exist.
    if (deviceRepository.findByHardwareId(hardwareId).isPresent()) {
      throw new QAlreadyExistsException(
          "Device with hardware id '{0}' is already registered with the platform.",
          hardwareId);
    }

    // Create a keypair for the device to be registered.
    KeyPairResponse keyPairResponse = cryptoResourceV1.generateKeyPair();

    // Set the security keys for the new device.
    final DeviceKey deviceKey = new DeviceKey()
        .setPublicKey(cryptoResourceV1.publicKeyToPEM(
            keyPairResponse.getPublicKey()))
        .setPrivateKey(cryptoResourceV1.privateKeyToPEM(
            keyPairResponse.getPrivateKey()))
        .setRolledOn(Instant.now())
        .setRolledAccepted(true);

    // Create a certificate for this device if the root CA is set.
    RegistryEntry deviceRootCA = registryResourceV1.findByName(
        Registry.DEVICE_ROOT_CA);
    if (deviceRootCA != null) {
      deviceKey.setCertificate(
          cryptoResourceV1.generateCertificateAsPEM(
              new CertificateRequest().setCn(hardwareId)
                  .setKeyPairResponse(keyPairResponse)));
      deviceKey.setCertificateCaId(
          registryResourceV1.findByName(Registry.DEVICE_ROOT_CA).asString());
    }

    // Create the new device.
    final Device device = new Device()
        .setHardwareId(hardwareId)
        .setState(state)
        .setDeviceKey(deviceKey);

    // Set device-pushed tags.
    if (!tags.isEmpty()) {
      checkTags(tags);
      device.setTags(tags);
    }

    deviceRepository.persist(device);
  }

  /**
   * Register a new device into the system. This method checks the currently
   * active registration mode of the platform and decides accordingly which
   * registration process to follow.
   *
   * @param registrationRequest The details of the registration for the device.
   * @param hardwareId          The hardware Id of the device to be registered.
   */
  public void register(DeviceMessage<RegistrationRequest> registrationRequest,
      String hardwareId)
  throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
         OperatorCreationException, NoSuchProviderException {

//    // Proceed with device registration.
//    if (srs.is(REGISTRATION_MODE, DISABLED)) {
//      throw new QDisabledException(
//          "Attempting to register device with hardware Id ''{0}'' but registration of new devices is "
//              + "disabled.", hardwareId);
//    } else {
//      // Check registration preconditions and register device.
//      log.log(Level.FINEST, "Platform running on ''{0}'' registration mode.",
//          srs.get(REGISTRATION_MODE));
//      log.log(Level.FINE,
//          "Attempting to register device with registration Id ''{0}'' and tags ''{1}''.",
//          ArrayUtils.toArray(hardwareId,
//              registrationRequest.getPayload().getTags()));
//      switch (srs.get(REGISTRATION_MODE)) {
//        case RegistrationMode.OPEN:
//          register(hardwareId, registrationRequest.getPayload().getTags(),
//              State.REGISTERED);
//          break;
//        case RegistrationMode.OPEN_WITH_APPROVAL:
//          register(hardwareId, registrationRequest.getPayload().getTags(),
//              State.APPROVAL);
//          break;
//        case RegistrationMode.ID:
//          activatePreregisteredDevice(hardwareId);
//          break;
//        case RegistrationMode.DISABLED:
//          throw new QDisabledException("Device registration is disabled.");
//        default:
//          throw new QDoesNotExistException(
//              "The requested registration mode does not exist.");
//      }
//    }
  }

  /**
   * Finds the cryptographic keys associated with a device.
   *
   * @param deviceId The device Id to find the keys of.
   * @return The cryptographic keys of the device.
   */
  public DeviceKey findKeys(ObjectId deviceId) {
//    return deviceKeyMapper.map(
//        deviceKeyRepository.findLatestAccepted(deviceId));
    return null;
  }

  /**
   * Finds devices performing a partial (i.e. wildcard) search by its hardware
   * Id.
   *
   * @param hardwareId The hardware Id to search by.
   * @return A list of devices found without having their cryptographic keys
   * populated.
   */
  public List<Device> findByPartialHardwareId(String hardwareId) {
//    return deviceMapper
//        .map(deviceRepository.findByHardwareIdContainsOrderByHardwareId(
//            hardwareId));
    return null;
  }

  /**
   * Finds a device based on its hardware Id.
   *
   * @param hardwareId The hardware Id to search by.
   * @return The device details with its cryptographic keys populated.
   */
  public Device findByHardwareId(String hardwareId) {
//    final Device Device = deviceMapper
//        .map(ReturnOptional.r(deviceRepository.findByHardwareId(hardwareId),
//            hardwareId));
//    fillCryptoKeys(Device);
//
//    return Device;

    return null;
  }

  /**
   * Finds the device entity class.
   *
   * @param hardwareId The hardware Id to search the device by.
   * @return Returns the Device as an {@link Optional}.
   */
  public Optional<Device> findEntityByHardwareId(String hardwareId) {
//    return deviceRepository.findByHardwareId(hardwareId);
    return null;
  }

  /**
   * Counts the devices in a list of hardware Ids. Search takes place via an
   * exact match algorithm.
   *
   * @param hardwareIds The list of hardware Ids to check.
   * @return The number of the devices in the list that matched.
   */
  public int countByHardwareIds(List<String> hardwareIds) {
    return findByHardwareIds(hardwareIds).size();
  }

  /**
   * Finds the devices in a list of hardware Ids. Search takes place via an
   * exact match algorithm.
   *
   * @param hardwareIds The list of hardware Ids to check.
   * @return Returns the list of devices matched.
   */
  public List<Device> findByHardwareIds(List<String> hardwareIds) {
//    return hardwareIds.stream()
//        .map(deviceRepository::findByHardwareId)
//        .filter(Optional::isPresent)
//        .map(Optional::get)
//        .map(deviceMapper::map)
//        .collect(Collectors.toList());
    return null;
  }

  /**
   * Counts the number of devices in the specific list of tags.
   *
   * @param tags The list of tags to search by.
   * @return Returns the number of devices matched.
   */
  public int countByTags(List<String> tags) {
    return findByTags(tags).size();
  }

  /**
   * Finds the devices matched by the specific list of tags.
   *
   * @param tags The list of tags to search by.
   * @return Returns the devices matched.
   */
  public List<Device> findByTags(String[] tags) {
    return findByTags(Arrays.asList(tags));
  }

  /**
   * Finds the devices matched by the specific list of tags.
   *
   * @param tags The list of tags to search by.
   * @return Returns the devices matched.
   */
  public List<Device> findByTags(List<String> tags) {
//    if (tags.isEmpty()) {
//      return new ArrayList<>();
//    } else {
//      List<Tag> tagsByName = Lists.newArrayList(
//          tagService.findAllByNameIn(tags));
//      List<Device> devices = deviceRepository.findByTagsIdIn(tagsByName.stream()
//          .map(Tag::getId).collect(Collectors.toList()));
//      return deviceMapper.map(devices);
//    }
    return null;
  }

  /**
   * Finds a device by its Id, optionally populating its cryptographic keys.
   *
   * @param id          The Id of the device to find.
   * @param processKeys Whether to populate the device's cryptographic keys or
   *                    not.
   * @return Returns the device matched.
   */
  public Device findById(ObjectId id, boolean processKeys) {
    final Device Device = super.findById(id);
    if (processKeys) {
      fillCryptoKeys(Device);
    }

    return Device;
  }

  /**
   * Delete a device by its Id
   *
   * @param id The Device Id to delete by.
   * @return Returns the details of the device deleted.
   */
//  @Override
//  public Device deleteById(long id) {
//    return super.deleteById(id);
//  }

  /**
   * Returns all non-hidden telemetry and metadata fields to be displayed on the
   * device page.
   *
   * @param deviceId The device Id to fetch fata for.
   * @return A list of device page data.
   */
  public List<DevicePage> getDevicePageData(long deviceId) {
//    final Device Device = findById(deviceId);
//
//    // Iterate over the available data types to find which fields to fetch.
//    return devicePageService.findAll()
//        .stream()
//        .filter(DevicePageDTO::isShown)
//        .map(field -> {
//          final String fieldValue = dtService
//              .executeMetadataOrTelemetry(
//                  DigitalTwins.Type.valueOf(field.getDatatype().toLowerCase()),
//                  Device.getHardwareId(), DTOperations.QUERY,
//                  field.getMeasurement(), field.getField(),
//                  null, null, 1, 1);
//          try {
//            @SuppressWarnings("unchecked") Map<String, List<Map<String, Object>>> jsonFields =
//                mapper.readValue(fieldValue, HashMap.class);
//            field.setMeasurement(jsonFields.keySet().iterator().next());
//            final List<Map<String, Object>> fields = jsonFields.get(
//                field.getMeasurement());
//            if (!fields.isEmpty()) {
//              String valueField = jsonFields.get(field.getMeasurement()).get(0)
//                  .keySet().stream()
//                  .filter(
//                      f -> !(f.equals(QueryResults.TIMESTAMP) || f.equals(
//                          QueryResults.TYPE)))
//                  .collect(Collectors.joining());
//              field.setField(valueField);
//              field.setValue(jsonFields.get(field.getMeasurement()).get(0)
//                  .get(valueField));
//              field.setLastUpdatedOn(Instant.ofEpochMilli(
//                  Long.parseLong(
//                      jsonFields.get(field.getMeasurement()).get(0)
//                          .get(QueryResults.TIMESTAMP)
//                          .toString())));
//            }
//          } catch (JsonProcessingException e) {
//            log.log(Level.SEVERE,
//                MessageFormat.format(
//                    "Could not obtain field values for device {0}.", deviceId),
//                e);
//          }
//          return field;
//        })
//        .collect(Collectors.toList());
    return null;
  }

  /**
   * Returns the last value of a specific telemetry or metadata field for a
   * device.
   *
   * @param deviceId The Id of the device to fetch the field value for.
   * @param field    The name of the telemetry or metadata field to fetch. The
   *                 field needs to follow the following format:
   *                 TYPE.MEASUREMENT.FIELD For example,
   *                 TELEMETRY.geolocation.latitude
   * @return The details of the device page.
   */
  public DevicePage getDeviceDataField(long deviceId, String field) {
//    final DevicePage devicePageDTO = new DevicePage();
//
//    // Split the field to each individual identifying components.
//    final String[] splitField = field.split("\\.");
//    String dataType = splitField[0];
//    String measurement = splitField[1];
//    String fieldName = splitField[2];
//    devicePageDTO.setField(fieldName).setMeasurement(measurement);
//
//    // Find the hardware Id of the device.
//    String hardwareId = findById(deviceId).getHardwareId();
//
//    // Fetch field value.
//    final String fieldValue = dtService
//        .executeMetadataOrTelemetry(
//            DigitalTwins.Type.valueOf(dataType.toLowerCase()), hardwareId,
//            DTOperations.QUERY, measurement, fieldName, null, null, 1, 1);
//    if (StringUtils.isNotBlank(fieldValue) && !fieldValue.trim().equals("{}")) {
//      try {
//        @SuppressWarnings("unchecked")
//        Map<String, List<Map<String, Object>>> jsonFields = mapper
//            .readValue(fieldValue, HashMap.class);
//        devicePageDTO.setValue(
//            jsonFields.get(measurement).get(0).get(fieldName).toString());
//        devicePageDTO.setLastUpdatedOn(Instant.ofEpochMilli(
//            (long) jsonFields.get(measurement).get(0).get("timestamp")));
//      } catch (JsonProcessingException e) {
//        throw new QMismatchException("Could not process field value.", e);
//      }
//    }
//
//    return devicePageDTO;
    return null;
  }

  /**
   * Removes a tag from all devices having it assigned to them.
   *
   * @param tagName the name of the tag to be removed.
   */
  @Transactional
  public void removeTag(String tagName) {
    log.debug("Removing tag '{}' from all devices.", tagName);
    deviceRepository.find("tags", tagName).stream()
        .forEach(device -> {
          device.getTags().removeIf(s -> s.equals(tagName));
          deviceRepository.update(device);
          log.trace("Removed tag '{}' from device '{}'.", tagName,
              device.getId());
        });
  }
}
