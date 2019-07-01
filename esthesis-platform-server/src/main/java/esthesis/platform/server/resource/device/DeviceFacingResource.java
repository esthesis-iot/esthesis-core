package esthesis.platform.server.resource.device;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.extension.device.DeviceMessage;
import esthesis.extension.device.dto.MQTTServer;
import esthesis.extension.device.request.ProvisioningInfoRequest;
import esthesis.extension.device.request.ProvisioningRequest;
import esthesis.extension.device.request.RegistrationRequest;
import esthesis.extension.device.response.ProvisioningInfoResponse;
import esthesis.extension.device.response.RegistrationResponse;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.config.AppSettings.Setting.Provisioning;
import esthesis.platform.server.config.AppSettings.SettingValues.Provisioning.ENCRYPTION;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.dto.ProvisioningDTO;
import esthesis.platform.server.mapper.ProvisioningMapper;
import esthesis.platform.server.service.DeviceService;
import esthesis.platform.server.service.MQTTService;
import esthesis.platform.server.service.ProvisioningService;
import esthesis.platform.server.service.SecurityService;
import esthesis.platform.server.service.SettingResolverService;
import esthesis.platform.server.service.TagService;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Validated
@RestController
@RequestMapping("/device")
public class DeviceFacingResource {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DeviceFacingResource.class.getName());

  private final DeviceService deviceService;
  private final SettingResolverService srs;
  private final MQTTService mqttService;
  private final SecurityService securityService;
  private final ProvisioningService provisioningService;
  private final AppProperties appProperties;
  private final ProvisioningMapper provisioningMapper;
  private final TagService tagService;

  public DeviceFacingResource(DeviceService deviceService,
    SettingResolverService srs, MQTTService mqttService,
    SecurityService securityService,
    ProvisioningService provisioningService,
    AppProperties appProperties,
    ProvisioningMapper provisioningMapper, TagService tagService) {
    this.deviceService = deviceService;
    this.srs = srs;
    this.mqttService = mqttService;
    this.securityService = securityService;
    this.provisioningService = provisioningService;
    this.appProperties = appProperties;
    this.provisioningMapper = provisioningMapper;
    this.tagService = tagService;
  }

  @PostMapping(path = "/register")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not register device.")
  public DeviceMessage<RegistrationResponse> register(
    @Valid @RequestBody DeviceMessage<RegistrationRequest> registrationRequest)
  throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
         BadPaddingException, InvalidKeyException, IOException,
         InvalidAlgorithmParameterException, InvalidKeySpecException, SignatureException {
    // Register the device.
    deviceService.register(registrationRequest, registrationRequest.getHardwareId());

    // Fetch the just-registered device to also obtain its keys.
    DeviceDTO deviceDTO = deviceService.findByHardwareId(registrationRequest.getHardwareId());

    // Prepare registration reply.
    DeviceMessage<RegistrationResponse> registrationReply = new DeviceMessage<>(
      appProperties.getNodeId());
    registrationReply.setPayload(new RegistrationResponse()
      .setPublicKey(deviceDTO.getPublicKey())
      .setPrivateKey(deviceDTO.getPrivateKey())
      .setSessionKey(deviceDTO.getSessionKey())
      .setPsPublicKey(deviceDTO.getPsPublicKey())
      .setProvisioningUrl(srs.get(Provisioning.URL))
      .setProvisioningKey(deviceDTO.getProvisioningKey())
    );

    // Find the MQTT server to send back to the device.
    Optional<MQTTServerDTO> mqttServerDTO = mqttService.matchByTag(deviceDTO.getTags());
    if (mqttServerDTO.isPresent()) {
      registrationReply.getPayload().setMqttServer(new MQTTServer()
        .setIpAddress(mqttServerDTO.get().getIpAddress()));
    } else {
      LOGGER.log(Level.WARNING, "Could not find a matching MQTT server for device {0} during "
        + "registration.", registrationRequest.getHardwareId());
    }

    // Sign and/or encrypt the reply according to preferences.
    securityService.prepareOutgoingMessage(registrationReply, deviceDTO);

    return registrationReply;
  }

  @PostMapping({"/provisioning/info/{id}", "/provisioning/info"})
  public ResponseEntity<DeviceMessage<ProvisioningInfoResponse>> provisioningInfo(
    @PathVariable Optional<Long> id,
    @Valid @RequestBody DeviceMessage<ProvisioningInfoRequest> provisioningInfoRequest)
  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException,
         SignatureException, InvalidAlgorithmParameterException, InvalidKeySpecException {
    // Find device information.
    final DeviceDTO deviceDTO = deviceService
      .findByHardwareId(provisioningInfoRequest.getHardwareId());

    // Verify signature and decrypt according to the configuration.
    securityService
      .processIncomingMessage(provisioningInfoRequest, ProvisioningInfoRequest.class, deviceDTO);

    // Case 1: If a specific ID is requested, obtain information for that specific provisioning
    // package. For information to be returned, the package must be active and matching the tags of
    // the device.
    // Case 2: When an ID is not specified, an active, tags-matching provisioning package is
    // returned. In there are multiple provisioning package candidates the one with the
    // latest packageVersion (based on String.compareTo) is returned.
    DeviceMessage<ProvisioningInfoResponse> provisioningInfoResponse = new DeviceMessage<>(
      appProperties.getNodeId());
    ProvisioningDTO provisioningDTO;
    if (!id.isPresent()) {
      Optional<ProvisioningDTO> optionalProvisioningDTO = provisioningService.matchByTag(deviceDTO);
      if (!optionalProvisioningDTO.isPresent()) {
        LOGGER.log(Level.FINE, "Could not find a matching provisioning "
          + "package for device {0}.", provisioningInfoRequest.getHardwareId());
        return ResponseEntity.ok().body(provisioningInfoResponse);
      } else {
        provisioningDTO = optionalProvisioningDTO.get();
      }
    } else {
      provisioningDTO = provisioningService.findById(id.get());
      //TODO checks active + tags (read comment above).
    }

    // If not provisioning package found, return empty, otherwise return the details of the
    // available provisioning package.
    if (provisioningDTO == null) {
      LOGGER.log(Level.FINEST, "Device {0} requested provisioning package but no package matched.",
        provisioningInfoRequest.getHardwareId());
      return ResponseEntity.ok().body(provisioningInfoResponse);
    } else {
      // Prepare and return the details of the provisioning package found.
      provisioningInfoResponse
        .setPayload(new ProvisioningInfoResponse()
          .setId(provisioningDTO.getId())
          .setDescription(provisioningDTO.getDescription())
          .setName(provisioningDTO.getName())
          .setPackageVersion(provisioningDTO.getPackageVersion())
          .setSha256(provisioningDTO.getSha256())
          .setSignature(srs.is(Provisioning.ENCRYPTION, ENCRYPTION.ENCRYPTED) ?
            provisioningDTO.getSignatureEncrypted() : provisioningDTO.getSignaturePlain())
          .setFileSize(provisioningDTO.getFileSize())
          .setFileName(provisioningDTO.getFileName()));
    }

    // Sign and/or encrypt.
    securityService.prepareOutgoingMessage(provisioningInfoResponse, deviceDTO);

    return ResponseEntity.ok().body(provisioningInfoResponse);
  }

  @PostMapping(value = "/provisioning/download/{id}")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not download "
    + "provisioning package.")
  public ResponseEntity provisioningDownload(@PathVariable long id,
    @Valid @RequestBody DeviceMessage<ProvisioningRequest> provisioningRequest)
  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException,
         SignatureException, InvalidAlgorithmParameterException, InvalidKeySpecException {
    // Find device information.
    final DeviceDTO deviceDTO = deviceService
      .findByHardwareId(provisioningRequest.getHardwareId());

    // Verify signature and decrypt according to the configuration.
    securityService
      .processIncomingMessage(provisioningRequest, ProvisioningRequest.class, deviceDTO);

    final ProvisioningDTO provisioningDTO = provisioningService.findById(id);
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + provisioningDTO.getFileName() + (
          srs.is(Provisioning.ENCRYPTION, ENCRYPTION.ENCRYPTED) ? ".encrypted" : ""
        ))
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(new InputStreamResource(
        provisioningService.download(id, srs.is(Provisioning.ENCRYPTION, ENCRYPTION.ENCRYPTED))));
  }
}
