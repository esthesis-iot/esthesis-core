package esthesis.service.application.resource;

import esthesis.common.AppConstants;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "ApplicationSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface ApplicationSystemResource {

	@GET
	@Path("/v1/system/is-token-valid")
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	boolean isTokenValid(@QueryParam("token") String token);

}
