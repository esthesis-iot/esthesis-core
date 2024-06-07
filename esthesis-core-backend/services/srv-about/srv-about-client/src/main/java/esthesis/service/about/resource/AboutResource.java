package esthesis.service.about.resource;

import esthesis.common.AppConstants;
import esthesis.service.about.dto.AboutGeneralDTO;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "AboutResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface AboutResource {

	@GET
	@Path("/v1/general")
	@RolesAllowed(AppConstants.ROLE_USER)
	AboutGeneralDTO getGeneralInfo();

}
