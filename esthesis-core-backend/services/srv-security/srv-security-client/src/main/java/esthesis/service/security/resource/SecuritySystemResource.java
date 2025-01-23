package esthesis.service.security.resource;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.security.dto.StatsDTO;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the security system service, when accessed by SYSTEM OIDC clients.
 */
@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "SecuritySystemResource")
public interface SecuritySystemResource {

	/**
	 * Check if a user is permitted to perform an operation on a resource.
	 *
	 * @param category   the category of the resource.
	 * @param operation  the operation.
	 * @param resourceId the resource id.
	 * @param userId     the user id.
	 * @return true if the user is permitted, false otherwise.
	 */
	@GET
	@Path("/v1/is-permitted")
	boolean isPermitted(@QueryParam("category") AppConstants.Security.Category category,
		@QueryParam("operation") Operation operation,
		@QueryParam("resourceId") String resourceId, @QueryParam("userId") ObjectId userId);

	/**
	 * Get statistics on security.
	 *
	 * @return the statistics.
	 */
	@GET
	@Path("/v1/stats")
	StatsDTO stats();

}
