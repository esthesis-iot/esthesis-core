package esthesis.service.device.resource;

import esthesis.service.device.dto.agent.RegistrationRequest;
import esthesis.service.device.dto.agent.RegistrationResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.bouncycastle.operator.OperatorCreationException;

//@Path("/api/v1/agent")
public interface AgentResourceV1 {

  @POST
  @Path(value = "/register")
  RegistrationResponse register(@Valid RegistrationRequest registrationRequest)
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
