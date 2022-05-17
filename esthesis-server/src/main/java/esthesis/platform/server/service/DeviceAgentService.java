package esthesis.platform.server.service;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.config.AppSettings.Setting.DeviceRegistration;
import esthesis.platform.server.config.AppSettings.Setting.Provisioning;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.dto.ProvisioningDTO;
import esthesis.platform.server.dto.device.DeviceDTO;
import esthesis.platform.server.dto.device.DeviceMessage;
import esthesis.platform.server.dto.device.MQTTServer;
import esthesis.platform.server.dto.device.ProvisioningInfoRequest;
import esthesis.platform.server.dto.device.ProvisioningInfoResponse;
import esthesis.platform.server.dto.device.ProvisioningRequest;
import esthesis.platform.server.dto.device.RegistrationRequest;
import esthesis.platform.server.dto.device.RegistrationResponse;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Level;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Device-facing functionality, handling provisioning and device registration services.
 */
@Log
@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class DeviceAgentService {

  private final DeviceService deviceService;
  private final SettingResolverService srs;
  private final MQTTService mqttService;
  private final ProvisioningService provisioningService;
  private final AppProperties appProperties;
  private final SettingResolverService settingResolverService;
  private final CAService caService;

  /**
   * Registers a new device into the system.
   */
  public DeviceMessage<RegistrationResponse> register(
    DeviceMessage<RegistrationRequest> registrationRequest)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException,
    InvalidAlgorithmParameterException, InvalidKeySpecException, SignatureException,
    OperatorCreationException, NoSuchProviderException {
    // Register the device.
    deviceService.register(registrationRequest, registrationRequest.getHardwareId());

    // Fetch the just-registered device to also obtain its keys.
    DeviceDTO deviceDTO = deviceService.findByHardwareId(registrationRequest.getHardwareId());

    // Find the root CA to be pushed to the device.
    String rootCACertificate = null;
    try {
      rootCACertificate = caService
        .findEntityById(settingResolverService.getAsLong(DeviceRegistration.ROOT_CA))
        .getCertificate();
    } catch (QDoesNotExistException e) {
      // Ignore if a root CA is not set yet.
    }

    // Prepare registration reply.
    DeviceMessage<RegistrationResponse> registrationReply = new DeviceMessage<>(
      appProperties.getNodeId());
    registrationReply.setPayload(new RegistrationResponse()
      .setPublicKey(deviceDTO.getPublicKey())
      .setPrivateKey(deviceDTO.getPrivateKey())
      .setProvisioningUrl(srs.get(Provisioning.URL))
      .setRootCaCertificate(rootCACertificate)
      .setCertificate(deviceDTO.getCertificate())
    );

    // Find the MQTT server to send back to the device.
    Optional<MQTTServerDTO> mqttServersDTO = mqttService
      .matchByTag(registrationRequest.getPayload().getTags());
    if (mqttServersDTO.isPresent()) {
      registrationReply.getPayload().setMqttServer(new MQTTServer()
        .setIpAddress(mqttServersDTO.get().getIpAddress()));
    } else {
      log.warning(MessageFormat.format(
        "Could not find a matching MQTT server for device {0} with tags {1} during "
          + "registration.",
        ArrayUtils.toArray(registrationRequest.getHardwareId(), deviceDTO.getTags())));
    }

    log.log(Level.FINE, "Registered device with hardware ID {0}.", deviceDTO.getHardwareId());
    return registrationReply;
  }

  /**
   * Checks for available downloads for device's provisioning.
   */
  public DeviceMessage<ProvisioningInfoResponse> provisioningInfo(
    Optional<Long> id, DeviceMessage<ProvisioningInfoRequest> provisioningInfoRequest) {
    // Find device information.
    final DeviceDTO deviceDTO = deviceService
      .findByHardwareId(provisioningInfoRequest.getHardwareId());

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
        log.log(Level.FINE, "Could not find a matching provisioning "
          + "package for device {0}.", provisioningInfoRequest.getHardwareId());
        return provisioningInfoResponse;
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
      log.log(Level.FINEST, "Device {0} requested provisioning package but no package matched.",
        provisioningInfoRequest.getHardwareId());
      return provisioningInfoResponse;
    } else {
      // Prepare and return the details of the provisioning package found.
      provisioningInfoResponse
        .setPayload(new ProvisioningInfoResponse()
          .setId(provisioningDTO.getId())
          .setDescription(provisioningDTO.getDescription())
          .setName(provisioningDTO.getName())
          .setPackageVersion(provisioningDTO.getPackageVersion())
          .setSha256(provisioningDTO.getSha256())
          .setFileSize(provisioningDTO.getFileSize())
          .setFileName(provisioningDTO.getFileName()));
    }

    return provisioningInfoResponse;
  }

  /**
   * Returns a stream with a provisioning package.
   */
  public InputStreamResource provisioningDownload(@PathVariable long id,
    @Valid @RequestBody DeviceMessage<ProvisioningRequest> provisioningRequest)
    throws IOException {
    // Find device information.
    final DeviceDTO deviceDTO = deviceService
      .findByHardwareId(provisioningRequest.getHardwareId());

    return new InputStreamResource(provisioningService.download(id));
  }
}
