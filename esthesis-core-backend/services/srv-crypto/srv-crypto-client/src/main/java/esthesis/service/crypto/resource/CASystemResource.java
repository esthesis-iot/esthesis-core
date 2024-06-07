package esthesis.service.crypto.resource;

import esthesis.common.AppConstants;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/system-ca")
@RegisterRestClient(configKey = "CASystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface CASystemResource {

	@GET
	@Path("/v1/{caId}/certificate")
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	String getCACertificate(@PathParam("caId") String caId);
}
