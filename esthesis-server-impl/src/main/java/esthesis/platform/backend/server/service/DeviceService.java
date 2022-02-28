package esthesis.platform.backend.server.service;

import static esthesis.platform.backend.server.config.AppSettings.Setting.DeviceRegistration.REGISTRATION_MODE;
import static esthesis.platform.backend.server.config.AppSettings.SettingValues.DeviceRegistration.RegistrationMode.DISABLED;

import com.eurodyn.qlack.common.exception.QAlreadyExistsException;
import com.eurodyn.qlack.common.exception.QDisabledException;
import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QMismatchException;
import com.eurodyn.qlack.common.exception.QSecurityException;
import com.eurodyn.qlack.fuse.crypto.dto.CertificateSignDTO;
import com.eurodyn.qlack.fuse.crypto.dto.CreateKeyPairDTO;
import com.eurodyn.qlack.fuse.crypto.service.CryptoAsymmetricService;
import com.eurodyn.qlack.fuse.crypto.service.CryptoCAService;
import com.eurodyn.qlack.fuse.crypto.service.CryptoSymmetricService;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import esthesis.platform.backend.common.device.RegistrationRequest;
import esthesis.platform.backend.common.device.dto.DeviceDTO;
import esthesis.platform.backend.common.dto.DeviceMessage;
import esthesis.platform.backend.server.config.AppConstants.Device.State;
import esthesis.platform.backend.server.config.AppConstants.DigitalTwins;
import esthesis.platform.backend.server.config.AppConstants.DigitalTwins.DTOperations;
import esthesis.platform.backend.server.config.AppConstants.NiFi.QueryResults;
import esthesis.platform.backend.server.config.AppProperties;
import esthesis.platform.backend.server.config.AppSettings.Setting.DeviceRegistration;
import esthesis.platform.backend.server.config.AppSettings.SettingValues.DeviceRegistration.RegistrationMode;
import esthesis.platform.backend.server.dto.DeviceKeyDTO;
import esthesis.platform.backend.server.dto.DevicePageDTO;
import esthesis.platform.backend.server.dto.DeviceRegistrationDTO;
import esthesis.platform.backend.server.mapper.DeviceKeyMapper;
import esthesis.platform.backend.server.mapper.DeviceMapper;
import esthesis.platform.backend.server.model.Ca;
import esthesis.platform.backend.server.model.Device;
import esthesis.platform.backend.server.model.DeviceKey;
import esthesis.platform.backend.server.model.Tag;
import esthesis.platform.backend.server.repository.DeviceKeyRepository;
import esthesis.platform.backend.server.repository.DeviceRepository;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Log
@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class DeviceService extends BaseService<DeviceDTO, Device> {

  private final DeviceRepository deviceRepository;
  private final DeviceMapper deviceMapper;
  private final DeviceKeyMapper deviceKeyMapper;
  private final SettingResolverService srs;
  private final TagService tagService;
  private final DeviceKeyRepository deviceKeyRepository;
  private final AppProperties appProperties;
  private final CryptoAsymmetricService cryptoAsymmetricService;
  private final CryptoSymmetricService cryptoSymmetricService;
  private final CAService caService;
  private final CryptoCAService cryptoCAService;
  private final DevicePageService devicePageService;
  private final DTService dtService;
  private final ObjectMapper mapper;

  /**
   * Populates the cryptographic keys of a device.
   *
   * @param deviceDTO The DTO of the device to populate the keys into.
   */
  private DeviceDTO fillCryptoKeys(DeviceDTO deviceDTO) {
    final DeviceKey keys = deviceKeyRepository.findLatestAccepted(deviceDTO.getId());
    deviceDTO.setPrivateKey(keys.getPrivateKey());
    deviceDTO.setPublicKey(keys.getPublicKey());
    deviceDTO.setCertificate(keys.getCertificate());

    return deviceDTO;
  }

  /**
   * Checks if device-provided tags exist in the system and reports the ones that do not exist.
   *
   * @param tags The comma-separated list of tags to check.
   */
  private void checkTags(String tags) {
    for (String tag : tags.split(",")) {
      tag = tag.trim();
      if (!tagService.findByName(tag).isPresent()) {
        log
          .log(Level.WARNING, "Device-pushed tag {0} does not exist.", tag);
      }
    }
  }

  /**
   * Activate a preregistered device. There is no actual device registration taking place here as
   * the device already exists in system's database.
   *
   * @param hardwareId The hardware Id of the device to activate.
   */
  private void activatePreregisteredDevice(String hardwareId) {
    Optional<Device> optionalDevice = deviceRepository.findByHardwareId(hardwareId);

    // Check that a device with the same hardware ID is not already registered.
    if (optionalDevice.isPresent() && !optionalDevice.get().getState()
      .equals(State.PREREGISTERED)) {
      throw new QSecurityException(
        "Cannot register device with hardware ID {0} as it is already in {1} state.",
        hardwareId, optionalDevice.get().getState());
    } else if (!optionalDevice.isPresent()) {
      throw new QSecurityException(
        "Device with hardware ID {0} does not exist.", hardwareId);
    }

    // Find the device and set its status to registered.
    Device device = optionalDevice.get();
    device.setState(State.REGISTERED);
    deviceRepository.save(device);
  }

  /**
   * Preregisters a device, so that it can self-register later on.
   *
   * @param deviceRegistrationDTO The preregistration details of the device.
   */
  @Async
  public void preregister(DeviceRegistrationDTO deviceRegistrationDTO)
  throws NoSuchAlgorithmException, OperatorCreationException, InvalidKeySpecException,
         NoSuchProviderException, IOException {
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
    String tagNames = deviceRegistrationDTO.getTags().stream()
      .map(tagId -> tagService.findById(tagId).getName())
      .collect(Collectors.joining(","));

    // Register IDs.
    for (String hardwareId : idList) {
      register(hardwareId, tagNames, State.PREREGISTERED);
    }
  }

  /**
   * The internal registration handler, see {@link #register(DeviceMessage, String)}.
   *
   * @param hardwareId The hardware Id of the device to be registered.
   * @param tags       The tags associated with this device as a comma-separated list.
   * @param state      The initial state of the registration of the device.
   */
  private void register(String hardwareId, String tags, String state)
  throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, OperatorCreationException,
         NoSuchProviderException {
    // Create a keypair for the device to be registered.
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

    // Create the new device.
    final Device device = new Device()
      .setHardwareId(hardwareId)
      .setState(state);
    deviceRepository.save(device);

    // Create the security keys for the new device.
    final DeviceKey deviceKey = new DeviceKey()
      .setPublicKey(cryptoAsymmetricService.publicKeyToPEM(keyPair))
      .setPrivateKey(cryptoAsymmetricService.privateKeyToPEM(keyPair))
      .setRolledOn(Instant.now())
      .setRolledAccepted(true)
      .setDevice(device);

    // Create a certificate for this device if the root CA is set.
    if (StringUtils.isNotBlank(srs.get(DeviceRegistration.ROOT_CA))) {
      final Ca ca = caService.findEntityById(srs.getAsLong(DeviceRegistration.ROOT_CA));
      CertificateSignDTO signDTO = new CertificateSignDTO()
        .setLocale(Locale.US)
        .setPrivateKey(keyPair.getPrivate())
        .setPublicKey(keyPair.getPublic())
        .setSignatureAlgorithm(appProperties.getSecurityAsymmetricSignatureAlgorithm())
        .setSubjectCN(hardwareId)
        .setValidForm(Instant.now())
        .setValidTo(ca.getValidity())
        .setIssuerCN(ca.getCn())
        .setIssuerPrivateKey(cryptoAsymmetricService.pemToPrivateKey(ca.getPrivateKey(),
          appProperties.getSecurityAsymmetricKeyAlgorithm()));
      deviceKey.setCertificate(
        cryptoCAService.certificateToPEM(cryptoCAService.generateCertificate(signDTO)));
      deviceKey.setCertificateCaId(ca.getId());
    }

    deviceKeyRepository.save(deviceKey);

    // Set device-pushed tags.
    if (StringUtils.isNotBlank(tags)) {
      checkTags(tags);
      device
        .setTags(Lists.newArrayList(tagService.findAllByNameIn(Arrays.asList(tags.split(",")))));
    }
  }

  /**
   * Register a new device into the system. This method checks the currently active registration
   * mode of the platform and decides accordingly which registration process to follow.
   *
   * @param registrationRequest The details of the registration for the device.
   * @param hardwareId          The hardware Id of the device to be registered.
   */
  public void register(DeviceMessage<RegistrationRequest> registrationRequest, String hardwareId)
  throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
         OperatorCreationException, NoSuchProviderException {

    // Proceed with device registration.
    if (srs.is(REGISTRATION_MODE, DISABLED)) {
      throw new QDisabledException(
        "Attempting to register device with hardware Id ''{0}'' but registration of new devices is "
          + "disabled.", hardwareId);
    } else {
      // Check registration preconditions and register device.
      log.log(Level.FINEST, "Platform running on ''{0}'' registration mode.",
        srs.get(REGISTRATION_MODE));
      log.log(Level.FINE,
        "Attempting to register device with registration Id ''{0}'' and tags ''{1}''.",
        ArrayUtils.toArray(hardwareId, registrationRequest.getPayload().getTags()));
      switch (srs.get(REGISTRATION_MODE)) {
        case RegistrationMode.OPEN:
          register(hardwareId, registrationRequest.getPayload().getTags(), State.REGISTERED);
          break;
        case RegistrationMode.OPEN_WITH_APPROVAL:
          register(hardwareId, registrationRequest.getPayload().getTags(), State.APPROVAL);
          break;
        case RegistrationMode.ID:
          activatePreregisteredDevice(hardwareId);
          break;
        case RegistrationMode.DISABLED:
          throw new QDisabledException("Device registration is disabled.");
        default:
          throw new QDoesNotExistException("The requested registration mode does not exist.");
      }
    }
  }

  /**
   * Finds the cryptographic keyts associated with a device.
   *
   * @param deviceId The device Id to find the keys of.
   * @return The cryptographic keys of the device.
   */
  public DeviceKeyDTO findKeys(long deviceId) {
    return deviceKeyMapper.map(deviceKeyRepository.findLatestAccepted(deviceId));
  }

  /**
   * Finds devices performing a partial (i.e. wildcard) search by its hardware Id.
   *
   * @param hardwareId The hardware Id to search by.
   * @return A list of devices found without having their cryptographic keys populated.
   */
  public List<DeviceDTO> findByPartialHardwareId(String hardwareId) {
    return deviceMapper
      .map(deviceRepository.findByHardwareIdContainsOrderByHardwareId(hardwareId));
  }

  /**
   * Finds a device based on its hardware Id.
   *
   * @param hardwareId The hardware Id to search by.
   * @return The device details with its cryptographic keys populated.
   */
  public DeviceDTO findByHardwareId(String hardwareId) {
    final DeviceDTO deviceDTO = deviceMapper
      .map(ReturnOptional.r(deviceRepository.findByHardwareId(hardwareId), hardwareId));
    fillCryptoKeys(deviceDTO);

    return deviceDTO;
  }

  /**
   * Finds the device entity class.
   *
   * @param hardwareId The hardware Id to search the device by.
   * @return Returns the Device as an {@link Optional}.
   */
  public Optional<Device> findEntityByHardwareId(String hardwareId) {
    return deviceRepository.findByHardwareId(hardwareId);
  }

  /**
   * Counts the devices in a list of hardware Ids. Search takes place via an exact match algorithm.
   *
   * @param hardwareIds The list of hardware Ids to check.
   * @return The number of the devices in the list that matched.
   */
  public int countByHardwareIds(List<String> hardwareIds) {
    return findByHardwareIds(hardwareIds).size();
  }

  /**
   * Finds the devices in a list of hardware Ids. Search takes place via an exact match algorithm.
   *
   * @param hardwareIds The list of hardware Ids to check.
   * @return Returns the list of devices matched.
   */
  public List<DeviceDTO> findByHardwareIds(List<String> hardwareIds) {
    return hardwareIds.stream()
      .map(deviceRepository::findByHardwareId)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .map(deviceMapper::map)
      .collect(Collectors.toList());
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
  public List<DeviceDTO> findByTags(String[] tags) {
    return findByTags(Arrays.asList(tags));
  }

  /**
   * Finds the devices matched by the specific list of tags.
   *
   * @param tags The list of tags to search by.
   * @return Returns the devices matched.
   */
  public List<DeviceDTO> findByTags(List<String> tags) {
    if (tags.isEmpty()) {
      return new ArrayList<>();
    } else {
      List<Tag> tagsByName = Lists.newArrayList(tagService.findAllByNameIn(tags));
      List<Device> devices = deviceRepository.findByTagsIdIn(tagsByName.stream()
        .map(Tag::getId).collect(Collectors.toList()));
      return deviceMapper.map(devices);
    }
  }

  /**
   * Finds a device by its Id, optionally populating its cryptographic keys.
   *
   * @param id          The Id of the device to find.
   * @param processKeys Whether to populate the device's cryptographic keys or not.
   * @return Returns the device matched.
   */
  public DeviceDTO findById(long id, boolean processKeys) {
    final DeviceDTO deviceDTO = super.findById(id);
    if (processKeys) {
      fillCryptoKeys(deviceDTO);
    }

    return deviceDTO;
  }

  /**
   * Delete a device by its Id
   *
   * @param id The Device Id to delete by.
   * @return Returns the details of the device deleted.
   */
  @Async
  @Override
  public DeviceDTO deleteById(long id) {
    return super.deleteById(id);
  }

  /**
   * Returns all non-hidden telemetry and metadata fields to be displayed on the device page.
   *
   * @param deviceId The device Id to fetch fata for.
   * @return A list of device page data.
   */
  public List<DevicePageDTO> getDevicePageData(long deviceId) {
    final DeviceDTO deviceDTO = findById(deviceId);

    // Iterate over the available data types to find which fields to fetch.
    return devicePageService.findAll()
      .stream()
      .filter(DevicePageDTO::isShown)
      .map(field -> {
        final String fieldValue = dtService
          .executeMetadataOrTelemetry(DigitalTwins.Type.valueOf(field.getDatatype().toLowerCase()),
            deviceDTO.getHardwareId(), DTOperations.QUERY, field.getMeasurement(), field.getField(),
            null, null, 1, 1);
        try {
          @SuppressWarnings("unchecked") Map<String, List<Map<String, Object>>> jsonFields =
            mapper.readValue(fieldValue, HashMap.class);
          field.setMeasurement(jsonFields.keySet().iterator().next());
          final List<Map<String, Object>> fields = jsonFields.get(field.getMeasurement());
          if (!fields.isEmpty()) {
            String valueField = jsonFields.get(field.getMeasurement()).get(0).keySet().stream()
              .filter(
                f -> !(f.equals(QueryResults.TIMESTAMP) || f.equals(QueryResults.TYPE)))
              .collect(Collectors.joining());
            field.setField(valueField);
            field.setValue(jsonFields.get(field.getMeasurement()).get(0).get(valueField));
            field.setLastUpdatedOn(Instant.ofEpochMilli(
              Long.parseLong(
                jsonFields.get(field.getMeasurement()).get(0).get(QueryResults.TIMESTAMP)
                  .toString())));
          }
        } catch (JsonProcessingException e) {
          log.log(Level.SEVERE,
            MessageFormat.format("Could not obtain field values for device {0}.", deviceId), e);
        }
        return field;
      })
      .collect(Collectors.toList());
  }

  /**
   * Returns the last value of a specific telemetry or metadata field for a device.
   *
   * @param deviceId The Id of the device to fetch the field value for.
   * @param field    The name of the telemetry or metadata field to fetch. The field needs to follow
   *                 the following format: TYPE.MEASUREMENT.FIELD For example,
   *                 TELEMETRY.geolocation.latitude
   * @return The details of the device page.
   */
  public DevicePageDTO getDeviceDataField(long deviceId, String field) {
    final DevicePageDTO devicePageDTO = new DevicePageDTO();

    // Split the field to each individual identifying components.
    final String[] splitField = field.split("\\.");
    String dataType = splitField[0];
    String measurement = splitField[1];
    String fieldName = splitField[2];
    devicePageDTO.setField(fieldName).setMeasurement(measurement);

    // Find the hardware Id of the device.
    String hardwareId = findById(deviceId).getHardwareId();

    // Fetch field value.
    final String fieldValue = dtService
      .executeMetadataOrTelemetry(DigitalTwins.Type.valueOf(dataType.toLowerCase()), hardwareId,
        DTOperations.QUERY, measurement, fieldName, null, null, 1, 1);
    if (StringUtils.isNotBlank(fieldValue) && !fieldValue.trim().equals("{}")) {
      try {
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> jsonFields = mapper
          .readValue(fieldValue, HashMap.class);
        devicePageDTO.setValue(jsonFields.get(measurement).get(0).get(fieldName).toString());
        devicePageDTO.setLastUpdatedOn(Instant.ofEpochMilli(
          (long) jsonFields.get(measurement).get(0).get("timestamp")));
      } catch (JsonProcessingException e) {
        throw new QMismatchException("Could not process field value.", e);
      }
    }

    return devicePageDTO;
  }
}
