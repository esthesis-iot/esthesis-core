package esthesis.services.device.impl.service;

import esthesis.service.crypto.resource.CAResourceV1;
import esthesis.service.registry.resource.RegistryResourceV1;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class AgentService {

  @Inject
  DeviceService deviceService;

  @Inject
  @RestClient
  CAResourceV1 caResourceV1;

  @Inject
  @RestClient
  RegistryResourceV1 registryResourceV1;

  /**
   * Registers a new device into the system.
   */
  /*public DeviceMessage<RegistrationResponse> register(
      DeviceMessage<RegistrationRequest> registrationRequest)
  throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
         OperatorCreationException, NoSuchProviderException {
    // Register the device.
    deviceService.register(registrationRequest,
        registrationRequest.getHardwareId());

    // Fetch the just-registered device to also obtain its keys.
    Device device = deviceService.findByHardwareId(
        registrationRequest.getHardwareId());

    // Find the root CA to be pushed to the device.
    String rootCACertificate = null;
    try {
      rootCACertificate = caResourceV1
          .findById(
              registryResourceV1.findByName(Registry.DEVICE_ROOT_CA).asObjectId())
          .getCertificate();
    } catch (QDoesNotExistException e) {
      // Ignore if a root CA is not set yet.
    }

    // Prepare registration reply.
    DeviceMessage<RegistrationResponse> registrationReply = new DeviceMessage<>();
    registrationReply.setPayload(new RegistrationResponse()
        .setPublicKey(device.getPublicKey())
        .setPrivateKey(device.getPrivateKey())
        .setProvisioningUrl(
            registryResourceV1.findByName(Registry.PROVISIONING_URL).asString())
        .setRootCaCertificate(rootCACertificate)
        .setCertificate(device.getCertificate())
    );

    // Find the MQTT server to send back to the device.
    MQTTServer mqttServer = mqttService
        .matchByTag(registrationRequest.getPayload().getTags());
    if (mqttServersDTO.isPresent()) {
      registrationReply.getPayload().setMqttServer(new MQTTServer()
          .setIpAddress(mqttServersDTO.get().getIpAddress()));
    } else {
      log.warning(MessageFormat.format(
          "Could not find a matching MQTT server for device {0} with tags {1} during "
              + "registration.",
          ArrayUtils.toArray(registrationRequest.getHardwareId(),
              deviceDTO.getTags())));
    }

    log.log(Level.FINE, "Registered device with hardware ID {0}.",
        deviceDTO.getHardwareId());
    return registrationReply;
  }

  *//**
   * Checks for available downloads for device's provisioning.
   *//*
  public DeviceMessage<ProvisioningInfoResponse> provisioningInfo(
      Optional<Long> id,
      DeviceMessage<ProvisioningInfoRequest> provisioningInfoRequest) {
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
      Optional<ProvisioningDTO> optionalProvisioningDTO = provisioningService.matchByTag(
          deviceDTO);
      if (!optionalProvisioningDTO.isPresent()) {
        log.log(Level.FINE, "Could not find a matching provisioning "
                + "package for device {0}.",
            provisioningInfoRequest.getHardwareId());
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
      log.log(Level.FINEST,
          "Device {0} requested provisioning package but no package matched.",
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

  *//**
   * Returns a stream with a provisioning package.
   *//*
  public InputStreamResource provisioningDownload(@PathVariable long id,
      @Valid @RequestBody DeviceMessage<ProvisioningRequest> provisioningRequest)
  throws IOException {
    // Find device information.
    final DeviceDTO deviceDTO = deviceService
        .findByHardwareId(provisioningRequest.getHardwareId());

    return new InputStreamResource(provisioningService.download(id));
  }*/
}
