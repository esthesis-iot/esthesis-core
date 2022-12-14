package esthesis.service.agent.resource;

import esthesis.service.agent.dto.AgentRegistrationRequest;
import esthesis.service.agent.dto.AgentRegistrationResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.bouncycastle.operator.OperatorCreationException;

@Path("/api")
public interface AgentResource {

  @POST
  @Path(value = "/v1/agent/register")
  AgentRegistrationResponse register(
      @Valid AgentRegistrationRequest agentRegistrationRequest)
  throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
         OperatorCreationException, NoSuchProviderException;

  /**
   * Attempts to find a candidate provisioning package for the given hardware id.
   *
   * @param hardwareId The hardware id to find a provisioning package for.
   * @param token      TODO define
   */
  @GET
  @Path(value = "/v1/agent/provisioning/{hardwareId}/find")
  Response findProvisioningPackage(@PathParam("hardwareId") String hardwareId,
      @QueryParam("token") Optional<String> token);

}
