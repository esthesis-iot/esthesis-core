package esthesis.services.device.impl.resource;

import esthesis.common.AppConstants.Registry;
import esthesis.service.crypto.resource.CAResourceV1;
import esthesis.service.dataflow.resource.DataflowResourceV1;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.agent.RegistrationRequest;
import esthesis.service.device.dto.agent.RegistrationResponse;
import esthesis.service.device.resource.AgentResourceV1;
import esthesis.service.registry.dto.RegistryEntry;
import esthesis.service.registry.resource.RegistryResourceV1;
import esthesis.services.device.impl.service.DeviceService;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.inject.Inject;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
public class AgentResourceV1Impl implements AgentResourceV1 {

  @Inject
  DeviceService deviceService;

  @Inject
  @RestClient
  CAResourceV1 caResourceV1;

  @Inject
  @RestClient
  RegistryResourceV1 registryResourceV1;

  @Inject
  @RestClient
  DataflowResourceV1 dataflowResourceV1;

  public RegistrationResponse register(
      @Valid RegistrationRequest registrationRequest)
  throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
         OperatorCreationException, NoSuchProviderException {
    Device device = deviceService.register(registrationRequest);

    RegistrationResponse registrationResponse = new RegistrationResponse();
    registrationResponse.setCertificate(device.getCertificate());
    registrationResponse.setPublicKey(device.getPublicKey());
    registrationResponse.setPrivateKey(device.getPrivateKey());

    // Find the root CA to be pushed to the device.
    ObjectId rootCaId = registryResourceV1.findByName(Registry.DEVICE_ROOT_CA)
        .asObjectId();
    if (rootCaId == null) {
      log.warn("Root CA is not set.");
    } else {
      registrationResponse.setRootCaCertificate(
          caResourceV1.findById(rootCaId).getCertificate());
    }

    // Find the MQTT server to send back to the device.
    String mqttServer = dataflowResourceV1.matchMqttServerByTags(
        Arrays.stream(registrationRequest.getTags().split(",")).toList());
    if (StringUtils.isNotBlank(mqttServer)) {
      registrationResponse.setMqttServer(mqttServer);
    } else {
      log.warn("Could not find a matching MQTT server for device {} with "
              + "tags {} during registration.", registrationRequest.getHardwareId(),
          registrationRequest.getTags());
    }

    // Set provisioning URL.
    RegistryEntry provisioningUrl = registryResourceV1.findByName(
        Registry.PROVISIONING_URL);
    if (provisioningUrl != null) {
      registrationResponse.setProvisioningUrl(provisioningUrl.getValue());
    } else {
      log.warn("Provisioning URL is not set.");
    }

    return registrationResponse;
  }

  /*
  public DeviceMessage<ProvisioningInfoResponse> provisioningInfo(
      @PathParam(value = "id") String id,
      @Valid DeviceMessage<ProvisioningInfoRequest> provisioningInfoRequest) {
    return Response.ok(
        agentService.provisioningInfo(id, provisioningInfoRequest));
  }

  public Response provisioningDownload(@PathParam(value = "id") String id,
      @Valid DeviceMessage<ProvisioningRequest> provisioningRequest) {
    return Response.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + provisioningService.findById(id)
                .getFileName())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(agentService.provisioningDownload(id, provisioningRequest));
  }*/
}
