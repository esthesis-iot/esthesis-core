package esthesis.platform.server.resource.device;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.extension.device.DeviceMessage;
import esthesis.extension.device.request.ProvisioningInfoRequest;
import esthesis.extension.device.request.ProvisioningRequest;
import esthesis.extension.device.request.RegistrationRequest;
import esthesis.extension.device.response.ProvisioningInfoResponse;
import esthesis.extension.device.response.RegistrationResponse;
import esthesis.extension.dto.MQTTServer;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.config.AppSettings.Setting.Provisioning;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.dto.ProvisioningDTO;
import esthesis.platform.server.dto.TagDTO;
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
import org.springframework.http.HttpStatus;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Validated
@RestController
@RequestMapping("/device")
public class DeviceResource {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DeviceResource.class.getName());

  private final DeviceService deviceService;
  private final SettingResolverService srs;
  private final MQTTService mqttService;
  private final SecurityService securityService;
  private final ProvisioningService provisioningService;
  private final AppProperties appProperties;
  private final ProvisioningMapper provisioningMapper;
  private final TagService tagService;

  public DeviceResource(DeviceService deviceService,
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
    Optional<MQTTServerDTO> mqttServerDTO = mqttService.matchByTag(deviceDTO);
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

  /**
   * A generic `get` without an ID to download an initial provisioning package.
   */
  //  @GetMapping({"/provisioning", "/provisioning/{id}"})
  @PostMapping({"/provisioning/download/{id}"})
  public ResponseEntity provisioning(@PathVariable Optional<Long> id,
    @Valid @RequestBody DeviceMessage<ProvisioningRequest> provisioningRequest) {
    return ResponseEntity.ok().build();
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
      .processIncomingMessage(provisioningInfoRequest, ProvisioningRequest.class, deviceDTO);

    // If a specific ID is requested, obtain information for that specific provisioning package.
    // Note that in case no checks to the state nor tags of the provisioning package is performed.
    // When an ID is not specified, an active, tags-matching provisioning package is returned. In
    // case you have multiple provisioning package candidates the one with the latest packageVersion
    // (based on String.compareTo) is returned.
    DeviceMessage<ProvisioningInfoResponse> provisioningInfoResponse = new DeviceMessage<>(
      appProperties.getNodeId());
    if (id.isPresent()) {
      provisioningInfoResponse
        .setPayload(provisioningMapper
          .toProvisioningInfoResponse(provisioningService.findEntityById((id.get()))));
    } else {
      final Optional<ProvisioningDTO> provisioningDTO = provisioningService.matchByTag(deviceDTO);
      if (provisioningDTO.isPresent()) {
        provisioningInfoResponse
          .setPayload(new ProvisioningInfoResponse()
            .setId(provisioningDTO.get().getId())
            .setDescription(provisioningDTO.get().getDescription())
            .setSigned(provisioningDTO.get().isSigned())
            .setName(provisioningDTO.get().getName())
            .setPackageVersion(provisioningDTO.get().getPackageVersion())
            .setEncrypted(provisioningDTO.get().isEncrypted())
            .setSigned(provisioningDTO.get().isSigned()));
      } else {
        LOGGER.log(Level.FINEST, "Device {0} requested latest provisioning package with tags {1} "
          + "but no package matched.", new Object[]{provisioningInfoRequest.getHardwareId(),
          StreamSupport.stream(tagService.findAllById(deviceDTO.getTags()).spliterator(), true).map(
            TagDTO::getName).collect(Collectors.joining(","))});
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
      }
    }

    // Sign and/or encrypt.
    securityService.prepareOutgoingMessage(provisioningInfoRequest, deviceDTO);

    return ResponseEntity.ok().body(provisioningInfoResponse);
  }
}
