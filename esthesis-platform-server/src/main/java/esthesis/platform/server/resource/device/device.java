package esthesis.platform.server.resource.device;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.extension.device.DeviceMessage;
import esthesis.extension.device.request.ProvisioningRequest;
import esthesis.extension.device.request.RegistrationRequest;
import esthesis.extension.device.response.RegistrationResponse;
import esthesis.extension.dto.MQTTServer;
import esthesis.platform.server.config.AppSettings.Setting.Provisioning;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.service.DeviceService;
import esthesis.platform.server.service.MQTTService;
import esthesis.platform.server.service.SecurityService;
import esthesis.platform.server.service.SettingResolverService;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
import java.util.logging.Logger;

@Validated
@RestController
@RequestMapping("/device")
public class device {
  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(device.class.getName());

  private final DeviceService deviceService;
  private final SettingResolverService srs;
  private final MQTTService mqttService;
  private final SecurityService securityService;

  public device(DeviceService deviceService,
    SettingResolverService srs, MQTTService mqttService,
    SecurityService securityService) {
    this.deviceService = deviceService;
    this.srs = srs;
    this.mqttService = mqttService;
    this.securityService = securityService;
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
    DeviceMessage<RegistrationResponse> registrationReply = new DeviceMessage<>();
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
   * @return
   */
//  @GetMapping({"/provisioning", "/provisioning/{id}"})
  @GetMapping({"/provisioning/{id}"})
  public ResponseEntity provisioning(@PathVariable Optional<Long> id,
    @Valid @RequestBody DeviceMessage<ProvisioningRequest> provisioningRequest) {
    return ResponseEntity.ok().build();
  }

}
