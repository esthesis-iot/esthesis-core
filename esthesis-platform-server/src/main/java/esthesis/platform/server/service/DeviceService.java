package esthesis.platform.server.service;

import static esthesis.platform.server.config.AppSettings.Setting.DeviceRegistration.REGISTRATION_MODE;
import static esthesis.platform.server.config.AppSettings.SettingValues.DeviceRegistration.RegistrationMode.DISABLED;

import com.eurodyn.qlack.common.exception.QAlreadyExistsException;
import com.eurodyn.qlack.common.exception.QDisabledException;
import com.eurodyn.qlack.common.exception.QDoesNotExistException;
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
import esthesis.common.device.RegistrationRequest;
import esthesis.common.device.dto.DeviceDTO;
import esthesis.common.dto.DeviceMessage;
import esthesis.common.util.Base64E;
import esthesis.platform.server.config.AppConstants.Device.State;
import esthesis.platform.server.config.AppConstants.DigitalTwins.DTOperations;
import esthesis.platform.server.config.AppConstants.NiFiQueryResults;
import esthesis.platform.server.config.AppConstants.WebSocket.Topic;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.config.AppSettings.Setting.DeviceRegistration;
import esthesis.platform.server.config.AppSettings.Setting.Provisioning;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.config.AppSettings.SettingValues.DeviceRegistration.RegistrationMode;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.IncomingEncryption;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.IncomingSignature;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.OutgoingEncryption;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.OutgoingSignature;
import esthesis.platform.server.dto.DTDeviceDTO;
import esthesis.platform.server.dto.DeviceKeyDTO;
import esthesis.platform.server.dto.DevicePageDTO;
import esthesis.platform.server.dto.DeviceRegistrationDTO;
import esthesis.platform.server.dto.WebSocketMessageDTO;
import esthesis.platform.server.mapper.DTDeviceMapper;
import esthesis.platform.server.mapper.DeviceKeyMapper;
import esthesis.platform.server.mapper.DeviceMapper;
import esthesis.platform.server.model.Ca;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.model.DeviceKey;
import esthesis.platform.server.repository.DeviceKeyRepository;
import esthesis.platform.server.repository.DeviceRepository;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.java.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
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
import java.security.NoSuchProviderException;
import java.security.SignatureException;
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

@Log
@Service
@Validated
@Transactional
public class DeviceService extends BaseService<DeviceDTO, Device> {

  private final DeviceRepository deviceRepository;
  private final DeviceMapper deviceMapper;
  private final DTDeviceMapper dtDeviceMapper;
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
  private final CAService caService;
  private final CryptoCAService cryptoCAService;
  private final DevicePageService devicePageService;
  private final DTService dtService;
  private final ObjectMapper mapper;

  @SuppressWarnings("java:S107")
  public DeviceService(
    DeviceRepository deviceRepository, DeviceMapper deviceMapper,
    DTDeviceMapper dtDeviceMapper, DeviceKeyMapper deviceKeyMapper,
    WebSocketService webSocketService, SettingResolverService srs,
    TagService tagService,
    DeviceKeyRepository deviceKeyRepository,
    SecurityService securityService, AppProperties appProperties,
    CryptoAsymmetricService cryptoAsymmetricService,
    CryptoSymmetricService cryptoSymmetricService,
    CertificatesService certificatesService, CAService caService,
    CryptoCAService cryptoCAService,
    DevicePageService devicePageService, DTService dtService,
    ObjectMapper mapper) {
    this.deviceRepository = deviceRepository;
    this.deviceMapper = deviceMapper;
    this.dtDeviceMapper = dtDeviceMapper;
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
    this.caService = caService;
    this.cryptoCAService = cryptoCAService;
    this.devicePageService = devicePageService;
    this.dtService = dtService;
    this.mapper = mapper;
  }

  /**
   * Preregister a device, so that it can self-register later on.
   */
  @Async
  public void preregister(DeviceRegistrationDTO deviceRegistrationDTO)
  throws NoSuchAlgorithmException, IOException, InvalidKeyException,
         InvalidAlgorithmParameterException, NoSuchPaddingException, OperatorCreationException,
         InvalidKeySpecException, NoSuchProviderException {
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
      register(hardwareId, tagNames, State.PREREGISTERED, false);
    }
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
   */
  private void activatePreregisteredDevice(RegistrationRequest registrationRequest,
    String hardwareId) {
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
   * The internal registration handler.
   */
  private void register(String hardwareId, String tags, String state, boolean checkTags)
  throws NoSuchAlgorithmException, IOException, InvalidKeyException,
         InvalidAlgorithmParameterException, NoSuchPaddingException, OperatorCreationException,
         InvalidKeySpecException, NoSuchProviderException {
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
    final byte[] sessionKey = cryptoSymmetricService.generateKey(
      appProperties.getSecuritySymmetricKeySize(),
      appProperties.getSecuritySymmetricKeyAlgorithm()).getEncoded();
    log.log(Level.FINEST, "Device session key: {0}.", Arrays.toString(sessionKey));
    final DeviceKey deviceKey = new DeviceKey()
      .setPrivateKey(securityService.encrypt(cryptoAsymmetricService.privateKeyToPEM(keyPair)))
      .setPublicKey(cryptoAsymmetricService.publicKeyToPEM(keyPair))
      .setPsPublicKey(certificatesService.getPSPublicKey())
      .setSessionKey(securityService.encrypt(sessionKey))
      // Provisioning key is already encrypted in settings.
      .setProvisioningKey(srs.get(Provisioning.AES_KEY))
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
        .setIssuerPrivateKey(cryptoAsymmetricService.pemToPrivateKey(
          new String(securityService.decrypt(ca.getPrivateKey()), StandardCharsets.UTF_8),
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
   */
  public void register(DeviceMessage<RegistrationRequest> registrationRequest,
    String hardwareId)
  throws NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException,
         InvalidAlgorithmParameterException, InvalidKeySpecException, SignatureException,
         OperatorCreationException, NoSuchProviderException {
    DeviceDTO deviceDTO;

    // Verify signature and decrypt according to the configuration.
    try {
      deviceDTO = findByHardwareId(registrationRequest.getHardwareId());
      securityService
        .processIncomingMessage(registrationRequest, RegistrationRequest.class, deviceDTO);
    } catch (QDoesNotExistException e) {
      // If the device is not preregistered it is not possible to verify the signature nor to
      // decrypt its payload.
      if (srs.is(Security.INCOMING_SIGNATURE, IncomingSignature.SIGNED) ||
        srs.is(Security.INCOMING_ENCRYPTION, IncomingEncryption.ENCRYPTED)) {
        throw new SecurityException("Signature and/or decryption is not supported during "
          + "registration unless the device is preregistered with the platform.");
      }
    }

    // Before proceeding to registration, check that the platform is capable to sign and/or encrypt
    // the reply in case the device requests so.
    if (registrationRequest.getPayload().isRepliesSigned() && srs.is(Security.OUTGOING_SIGNATURE,
      OutgoingSignature.NOT_SIGNED)) {
      throw new SecurityException("Device registration request requires a signed registration "
        + "reply, however the platform operates in non-signing mode.");
    }
    if (registrationRequest.getPayload().isRepliesEncrypted() && srs
      .is(Security.OUTGOING_ENCRYPTION, OutgoingEncryption.NOT_ENCRYPTED)) {
      throw new SecurityException("Device registration request requires an encrypted "
        + "registration reply, however the platform operates in non-encryption mode.");
    }

    // Proceed with device registration.
    if (srs.is(REGISTRATION_MODE, DISABLED)) {
      throw new QDisabledException(
        "Attempting to register device with hardware ID {0} but registration of new devices is "
          + "disabled.", hardwareId);
    } else {
      // Check registration preconditions and register device.
      log.log(Level.FINEST, "Platform running on {0} registration mode.",
        srs.get(REGISTRATION_MODE));
      log.log(Level.FINE, "Attempting to register device with registration ID {0} and tags {1}.",
        ArrayUtils.toArray(hardwareId, registrationRequest.getPayload().getTags()));
      switch (srs.get(REGISTRATION_MODE)) {
        case RegistrationMode.OPEN:
          register(hardwareId, registrationRequest.getPayload().getTags(), State.REGISTERED, true);
          break;
        case RegistrationMode.OPEN_WITH_APPROVAL:
          register(hardwareId,
            registrationRequest.getPayload().getTags(), State.APPROVAL, true);
          break;
        case RegistrationMode.ID:
          activatePreregisteredDevice(registrationRequest.getPayload(), hardwareId);
          break;
        case RegistrationMode.DISABLED:
          throw new QDisabledException("Device registration is disabled.");
        default:
          throw new QDoesNotExistException("The requested registration mode does not exist.");
      }

      // Realtime notification.
      webSocketService.publish(new WebSocketMessageDTO()
        .setTopic(Topic.DEVICE_REGISTRATION)
        .setPayload(
          MessageFormat.format("Device with registration id {0} registered.", hardwareId)));
    }
  }

  private DeviceDTO fillDecryptedKeys(DeviceDTO deviceDTO) {
    final DeviceKey keys = deviceKeyRepository.findLatestAccepted(deviceDTO.getId());

    try {
      deviceDTO.setPrivateKey(new String(securityService.decrypt(keys.getPrivateKey()),
        StandardCharsets.UTF_8));
      deviceDTO.setPublicKey(keys.getPublicKey());
      deviceDTO.setPsPublicKey(keys.getPsPublicKey());
      deviceDTO.setSessionKey(
        Base64E.encode(securityService.decrypt(keys.getSessionKey())));
      deviceDTO.setProvisioningKey(
        Base64E.encode(securityService.decrypt(keys.getProvisioningKey())));
      deviceDTO.setCertificate(keys.getCertificate());
    } catch (NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException |
      InvalidAlgorithmParameterException | IOException e) {
      log.log(Level.SEVERE, "Could not obtain device\\'s cryptographic keys.", e);
    }

    return deviceDTO;
  }

  public DeviceKeyDTO findKeys(long deviceId) {
    return deviceKeyMapper.map(deviceKeyRepository.findLatestAccepted(deviceId));
  }

  public DeviceDTO findByHardwareId(String hardwareId) {
    final DeviceDTO deviceDTO = deviceMapper
      .map(ReturnOptional.r(deviceRepository.findByHardwareId(hardwareId), hardwareId));
    fillDecryptedKeys(deviceDTO);

    return deviceDTO;
  }

  public int countByHardwareIds(List<String> hardwareIds) {
    return findByHardwareIds(hardwareIds).size();
  }

  public List<DeviceDTO> findByHardwareIds(List<String> hardwareIds) {
    if (hardwareIds.isEmpty()) {
      return new ArrayList<>();
    } else {
      List<DeviceDTO> devices = new ArrayList<>();
      for (String id : hardwareIds) {
        id = id.trim();
        devices.addAll(deviceMapper.map(deviceRepository.findByHardwareIdContains(id)));
      }
      return devices;
    }
  }

  public int countByTags(List<String> tags) {
    return findByTags(tags).size();
  }

  public List<DeviceDTO> findByTags(List<String> tags) {
    if (tags.isEmpty()) {
      return new ArrayList<>();
    } else {
      return deviceMapper.map(deviceRepository
        .findByTagsIn(
          Lists.newArrayList(tagService.findAllByNameIn(tags))));
    }
  }

  public DeviceDTO findById(long id, boolean processKeys) {
    final DeviceDTO deviceDTO = super.findById(id);
    if (processKeys) {
      fillDecryptedKeys(deviceDTO);
    }

    return deviceDTO;
  }

  public List<DTDeviceDTO> findAllDT() {
    return dtDeviceMapper.map(deviceRepository.findAll());
  }

  @Async
  @Override
  public DeviceDTO deleteById(long id) {
    return super.deleteById(id);
  }

  public List<DevicePageDTO> getFieldValues(long deviceId) {
    final DeviceDTO deviceDTO = findById(deviceId);

    // Iterate over the available data types to find which fields to fetch.
    return devicePageService.findAll()
      .stream()
      .filter(DevicePageDTO::isShown)
      .map(field -> {
        final String fieldValue = dtService
          .executeMetadataOrTelemetry(field.getDatatype().toLowerCase(), deviceDTO.getHardwareId(),
            DTOperations.OPERATION_QUERY.toLowerCase(), field.getMeasurement(), field.getField(),
            null, null, 1, 1);
        try {
          @SuppressWarnings("unchecked") Map<String, List<Map<String, Object>>> jsonFields =
            mapper.readValue(fieldValue, HashMap.class);
          field.setMeasurement(jsonFields.keySet().iterator().next());
          final List<Map<String, Object>> fields = jsonFields.get(field.getMeasurement());
          if (!fields.isEmpty()) {
            String valueField = jsonFields.get(field.getMeasurement()).get(0).keySet().stream()
              .filter(
                f -> !(f.equals(NiFiQueryResults.TIMESTAMP) || f.equals(NiFiQueryResults.TYPE)))
              .collect(Collectors.joining());
            field.setField(valueField);
            field.setValue(jsonFields.get(field.getMeasurement()).get(0).get(valueField));
            field.setLastUpdatedOn(Instant.ofEpochMilli(
              Long.parseLong(
                jsonFields.get(field.getMeasurement()).get(0).get(NiFiQueryResults.TIMESTAMP)
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

}
