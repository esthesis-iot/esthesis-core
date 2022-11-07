package esthesis.service.agent.resource;

import esthesis.service.agent.dto.AgentRegistrationRequest;
import esthesis.service.agent.dto.AgentRegistrationResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.bouncycastle.operator.OperatorCreationException;

@Path("/api")
public interface AgentResource {

  @POST
  @Path(value = "/v1/agent/register")
  AgentRegistrationResponse register(
      @Valid AgentRegistrationRequest agentRegistrationRequest)
  throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
         OperatorCreationException, NoSuchProviderException;

//  @POST
//  @Path(value = "/provisioning/info/{id}")
//  DeviceMessage<ProvisioningInfoResponse> provisioningInfo(
//      @PathParam(value = "id") String id,
//      @Valid DeviceMessage<ProvisioningInfoRequest> provisioningInfoRequest);
//
//  @POST
//  @Path(value = "/provisioning/download/{id}")
//  Response provisioningDownload(@PathParam(value = "id") String id,
//      @Valid DeviceMessage<ProvisioningRequest> provisioningRequest);
}
