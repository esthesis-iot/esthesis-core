package esthesis.service.agent.impl.service;

import esthesis.common.AppConstants.Registry;
import esthesis.service.agent.dto.AgentRegistrationRequest;
import esthesis.service.agent.dto.AgentRegistrationResponse;
import esthesis.service.crypto.resource.CASystemResource;
import esthesis.service.dataflow.dto.DataFlowMqttClientConfig;
import esthesis.service.dataflow.resource.DataflowSystemResource;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DeviceRegistration;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.registry.dto.RegistryEntry;
import esthesis.service.registry.resource.RegistrySystemResource;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class AgentService {

  @Inject
  @RestClient
  CASystemResource caSystemResource;

  @Inject
  @RestClient
  RegistrySystemResource registrySystemResource;

  @Inject
  @RestClient
  DataflowSystemResource dataflowSystemResource;

  @Inject
  @RestClient
  DeviceSystemResource deviceSystemResource;

  /**
   * Registers a new device into the system.
   */
  public AgentRegistrationResponse register(
      AgentRegistrationRequest agentRegistrationRequest)
  throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
         NoSuchProviderException, OperatorCreationException {
    DeviceRegistration deviceRegistration = new DeviceRegistration();
    deviceRegistration.setIds(agentRegistrationRequest.getHardwareId());
    if (StringUtils.isNotBlank(agentRegistrationRequest.getTags())) {
      deviceRegistration.setTags(
          Arrays.stream(agentRegistrationRequest.getTags().split(","))
              .toList());
    }
    log.debug("Requesting device registration with: '{}'", deviceRegistration);
    Device device = deviceSystemResource.register(deviceRegistration);

    AgentRegistrationResponse agentRegistrationResponse = new AgentRegistrationResponse();
    agentRegistrationResponse.setCertificate(
        device.getDeviceKey().getCertificate());
    agentRegistrationResponse.setPublicKey(
        device.getDeviceKey().getPublicKey());
    agentRegistrationResponse.setPrivateKey(
        device.getDeviceKey().getPrivateKey());

    // Find the root CA to be pushed to the device.
    ObjectId rootCaId =
        registrySystemResource.findByName(Registry.DEVICE_ROOT_CA).asObjectId();
    if (rootCaId == null) {
      log.warn("Root CA is not set.");
    } else {
      agentRegistrationResponse.setRootCaCertificate(
          caSystemResource.getCACertificate(rootCaId));
    }
    
    // Find the MQTT server to send back to the device.
    DataFlowMqttClientConfig mqttServer = dataflowSystemResource.matchMqttServerByTags(
        Arrays.asList(agentRegistrationRequest.getTags().split(",")));
    if (mqttServer != null) {
      agentRegistrationResponse.setMqttServer(mqttServer.getUrl());
    } else {
      log.warn("Could not find a matching MQTT server for device {} with "
              + "tags {} during registration.",
          agentRegistrationRequest.getHardwareId(),
          agentRegistrationRequest.getTags());
    }

    // Set provisioning URL.
    RegistryEntry provisioningUrl =
        registrySystemResource.findByName(Registry.DEVICE_PROVISIONING_URL);
    if (provisioningUrl != null) {
      agentRegistrationResponse.setProvisioningUrl(provisioningUrl.getValue());
    } else {
      log.warn("Provisioning URL is not set.");
    }

    return agentRegistrationResponse;
  }

  /*
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
