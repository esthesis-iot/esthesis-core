package esthesis.service.agent.impl.resource;

import esthesis.service.agent.dto.AgentRegistrationRequest;
import esthesis.service.agent.dto.AgentRegistrationResponse;
import esthesis.service.agent.impl.service.AgentService;
import esthesis.service.agent.resource.AgentResource;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.inject.Inject;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.operator.OperatorCreationException;

@Slf4j
public class AgentResourceImpl implements AgentResource {

  @Inject
  AgentService agentService;

  public AgentRegistrationResponse register(
      @Valid AgentRegistrationRequest agentRegistrationRequest)
  throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
         OperatorCreationException, NoSuchProviderException {
    return agentService.register(agentRegistrationRequest);
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
