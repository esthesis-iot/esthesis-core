package esthesis.service.application.resource;

import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "ApplicationSystemResource")
public interface ApplicationSystemResource {

	@GET
	@Path("/v1/system/is-token-valid")

	boolean isTokenValid(@QueryParam("token") String token);
}
