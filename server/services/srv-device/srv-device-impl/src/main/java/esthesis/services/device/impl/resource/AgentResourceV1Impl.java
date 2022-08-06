package esthesis.services.device.impl.resource;

import esthesis.service.device.resource.AgentResourceV1;

public class AgentResourceV1Impl implements AgentResourceV1 {

  /*@Inject
  AgentService agentService;

  public DeviceMessage<RegistrationResponse> register(
      @Valid DeviceMessage<RegistrationRequest> registrationRequest) {
    return agentService.register(registrationRequest);
  }

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
