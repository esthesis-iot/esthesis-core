package esthesis.service.provisioning.resource;

import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "ProvisioningAgentResource")
public interface ProvisioningAgentResource {

	@GET
	@Path("/v1/agent/find/{hardwareId}")
	ProvisioningPackageEntity find(@PathParam("hardwareId") String hardwareId,
		@QueryParam("version") String version);

	@GET
	@Path("/v1/agent/find/by-id/{provisioningPackageId}")
	ProvisioningPackageEntity findById(
		@PathParam("provisioningPackageId") String provisioningPackageId);
}
