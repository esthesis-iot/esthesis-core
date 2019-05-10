package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.github.slugify.Slugify;
import com.querydsl.core.types.Predicate;
import esthesis.extension.device.DeviceMessage;
import esthesis.extension.device.request.RegistrationRequest;
import esthesis.extension.device.response.RegistrationResponse;
import esthesis.extension.dto.MQTTServer;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.IncomingEncryption;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.IncomingSignature;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.OutgoingEncryption;
import esthesis.platform.server.config.AppSettings.SettingValues.Security.OutgoingSignature;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.DeviceKeyDTO;
import esthesis.platform.server.dto.DeviceRegistrationDTO;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.service.CertificatesService;
import esthesis.platform.server.service.DeviceService;
import esthesis.platform.server.service.MQTTService;
import esthesis.platform.server.service.SecurityService;
import esthesis.platform.server.service.SettingResolverService;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import org.apache.commons.codec.binary.Base64;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import java.util.logging.Logger;

@Validated
@RestController
@RequestMapping("/devices")
public class DevicesResource {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DevicesResource.class.getName());

  private final DeviceService deviceService;
  private final SettingResolverService srs;
  private final CertificatesService certificatesService;
  private final SecurityService securityService;
  private final MQTTService mqttService;

  public DevicesResource(DeviceService deviceService, SettingResolverService srs,
    CertificatesService certificatesService,
    SecurityService securityService, MQTTService mqttService) {
    this.deviceService = deviceService;
    this.srs = srs;
    this.certificatesService = certificatesService;
    this.securityService = securityService;
    this.mqttService = mqttService;
  }

  @PostMapping(path = "/preregister")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not register device(s).")
  public ResponseEntity preregister(@Valid @RequestBody DeviceRegistrationDTO deviceRegistrationDTO)
  throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException,
         NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException,
         InvalidKeyException {
    deviceService.preregister(deviceRegistrationDTO);

    return ResponseEntity.ok().build();
  }

  @EmptyPredicateCheck
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not obtain devices list.")
  @ReplyPageableFilter("createdOn,hardwareId,id,state")
  public Page<DeviceDTO> findAll(@QuerydslPredicate(root = Device.class) Predicate predicate,
    Pageable pageable) {
    return deviceService.findAll(predicate, pageable);
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch device.")
  @ReplyFilter("-certificate,-privateKey,-publicKey,-psPublicKey,-sessionKey")
  public DeviceDTO get(@PathVariable long id) {
    final DeviceDTO deviceDTO = deviceService.findById(id);

    return deviceDTO;
  }

  @PostMapping(path = "/register")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not register device.")
  public DeviceMessage<RegistrationResponse> register(
    @Valid @RequestBody DeviceMessage<RegistrationRequest> registrationRequest)
  throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
         BadPaddingException, InvalidKeyException, IOException,
         InvalidAlgorithmParameterException, InvalidKeySpecException, SignatureException {
    // Verify signature and decrypt according to the configuration.
    DeviceDTO deviceDTO;
    try {
      deviceDTO = deviceService.findByHardwareId(registrationRequest.getHardwareId());
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

    // Verify that the hardware ID on `DeviceMessage` is identical to the one in
    // `RegistrationRequest`. This provides device ID verification but only if your system is set
    // to require signing of incoming requests (and operate under "preregistered devices" device
    // registration mode.
    if (srs.is(Security.INCOMING_SIGNATURE, IncomingSignature.SIGNED) && !registrationRequest
      .getHardwareId().equals(registrationRequest.getPayload().getHardwareId())) {
      throw new SecurityException("Device ID in the payload does not match to the device ID of "
        + "the payload wrapper.");
    }

    // Before proceeding to registration, check that the platform is capable to sign and/or encrypt
    // the reply in case the device requests so.
    if (registrationRequest.getPayload().isRepliesSigned() && srs.is(Security.OUTGOING_SIGNATURE,
      OutgoingSignature.NOT_SIGNED)) {
      throw new SecurityException("Device registration request requires a signed registration "
        + "reply, however the platform operates in non-signing mode.");
    }
    if (registrationRequest.getPayload().isRepliesEncrypted() && srs
      .is(Security.OUTGOING_ENCRYPTION,
        OutgoingEncryption.NOT_ENCRYPTED)) {
      throw new SecurityException("Device registration request requires an encrypted "
        + "registration reply, however the platform operates in non-encryption mode.");
    }

    // Register the device.
    deviceDTO = deviceService
      .register(registrationRequest.getPayload(), registrationRequest.getHardwareId());

    // Prepare registration reply.
    DeviceMessage<RegistrationResponse> registrationReply = new DeviceMessage<>();
    registrationReply.setPayload(new RegistrationResponse()
      .setPublicKey(deviceDTO.getPublicKey())
      .setPrivateKey(deviceDTO.getPrivateKey())
      .setSessionKey(deviceDTO.getSessionKey())
      .setPsPublicKey(
        certificatesService.findById(srs.getAsLong(Security.PLATFORM_CERTIFICATE)).getPublicKey())
    );

    // Find the MQTT server to send back to the device.
    Optional<MQTTServerDTO> mqttServerDTO = mqttService.matchByTag(deviceDTO.getTags());
    if (mqttServerDTO.isPresent()) {
      registrationReply.getPayload().setMqttServer(new MQTTServer()
        .setIpAddress(mqttServerDTO.get().getIpAddress())
        .setTopicControl(mqttServerDTO.get().getTopicControl())
        .setTopicMetadata(mqttServerDTO.get().getTopicMetadata())
        .setTopicTelemetry(mqttServerDTO.get().getTopicTelemetry())
      );
    }

    // Sign and/or encrypt the reply according to preferences.
    securityService.prepareOutgoingMessage(registrationReply, deviceDTO);

    return registrationReply;
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete device.")
  public void delete(@PathVariable long id) {
    deviceService.deleteById(id);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save Device.")
  public DeviceDTO save(@Valid @RequestBody DeviceDTO object) {
    return deviceService.save(object);
  }

  @GetMapping(path = "{deviceId}/keys")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch device keys.")
  public ResponseEntity downloadKeys(@PathVariable long deviceId)
  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
         IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    // Prepare a filename for downloading.
    String filename =
      new Slugify().slugify(deviceService.findById(deviceId).getHardwareId()) + ".keys";

    // Get the keys and decrypt values.
    final DeviceKeyDTO deviceKeys = deviceService.findKeys(deviceId);
    deviceKeys
      .setPrivateKey(new String(securityService.decrypt(deviceKeys.getPrivateKey()),
        StandardCharsets.UTF_8));
    deviceKeys
      .setSessionKey(
        Base64.encodeBase64String(securityService.decrypt(deviceKeys.getSessionKey())));

    // Prepare the reply.
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append("PUBLIC KEY\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append(deviceKeys.getPublicKey());
    stringBuilder.append("\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append("PRIVATE KEY\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append(deviceKeys.getPrivateKey());
    stringBuilder.append("\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append("SESSION KEY\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append(deviceKeys.getSessionKey());
    stringBuilder.append("\n\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append("PLATFORM PUBLIC KEY\n");
    stringBuilder.append("****************************************************************\n");
    stringBuilder.append(deviceKeys.getPsPublicKey());

    return ResponseEntity
      .ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(stringBuilder.toString());
  }
}
