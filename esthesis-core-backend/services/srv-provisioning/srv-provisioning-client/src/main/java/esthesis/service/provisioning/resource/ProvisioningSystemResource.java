package esthesis.service.provisioning.resource;

import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the provisioning service, when accessed by a SYSTEM OIDC client.
 */
@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "ProvisioningSystemResource")
public interface ProvisioningSystemResource {

	/**
	 * Find the provisioning package for the given hardware ID and version.
	 *
	 * @param hardwareId The hardware ID.
	 * @param version    The version.
	 * @return The provisioning package, or null if not found.
	 */
	@GET
	@Path("/v1/system/find/{hardwareId}")
	ProvisioningPackageEntity find(@PathParam("hardwareId") String hardwareId,
		@QueryParam("version") String version);

	/**
	 * Find the provisioning package for the given provisioning package ID.
	 *
	 * @param provisioningPackageId The provisioning package ID.
	 * @return The provisioning package, or null if not found.
	 */
	@GET
	@Path("/v1/system/find/by-id/{provisioningPackageId}")
	ProvisioningPackageEntity findById(
		@PathParam("provisioningPackageId") String provisioningPackageId);
}
