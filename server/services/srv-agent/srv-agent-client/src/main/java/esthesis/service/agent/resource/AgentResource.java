package esthesis.service.agent.resource;

import esthesis.service.agent.dto.AgentRegistrationRequest;
import esthesis.service.agent.dto.AgentRegistrationResponse;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.bouncycastle.operator.OperatorCreationException;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api")
public interface AgentResource {

  @POST
  @Path(value = "/v1/register")
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
  @Path(value = "/v1/provisioning/find")
  Response findProvisioningPackage(
      @QueryParam("hardwareId") String hardwareId,
      @QueryParam("version") String version,
      @QueryParam("token") Optional<String> token);

  @GET
  @Path(value = "/v1/provisioning/find/by-id")
  Response findProvisioningPackageById(
      @QueryParam("hardwareId") String hardwareId,
      @QueryParam("packageId") String packageId,
      @QueryParam("token") Optional<String> token);

  @GET
  @Path("/v1/provisioning/download")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Uni<RestResponse<byte[]>> downloadProvisioningPackage(@QueryParam("token") String token);
}
