package esthesis.service.security.resource;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Security.Operation;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "SecuritySystemResource")
public interface SecuritySystemResource {

	@GET
	@Path("/v1/is-permitted")
	boolean isPermitted(@QueryParam("category") AppConstants.Security.Category category,
		@QueryParam("operation") Operation operation,
		@QueryParam("resourceId") String resourceId, @QueryParam("userId") ObjectId userId);

}
