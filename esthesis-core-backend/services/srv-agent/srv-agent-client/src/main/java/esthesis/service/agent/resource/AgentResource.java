package esthesis.service.agent.resource;

import esthesis.service.agent.dto.AgentRegistrationRequest;
import esthesis.service.agent.dto.AgentRegistrationResponse;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import org.bouncycastle.operator.OperatorCreationException;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api")
public interface AgentResource {
	@POST
	@Path(value = "/v1/register")
	AgentRegistrationResponse register(@Valid AgentRegistrationRequest agentRegistrationRequest)
	throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
				 OperatorCreationException, NoSuchProviderException;

	/**
	 * Attempts to find a candidate provisioning package for the given hardware id.
	 *
	 * @param hardwareId The hardware id to find a provisioning package for.
	 * @param token      An RSA/SHA256 digital signature of a SHA256 hashed version of the hardware id
	 *                   of the device requesting the information.
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
