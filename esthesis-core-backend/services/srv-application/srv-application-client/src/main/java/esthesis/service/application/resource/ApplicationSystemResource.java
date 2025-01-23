package esthesis.service.application.resource;

import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the application system service, for SYSTEM OIDC clients.
 */
@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "ApplicationSystemResource")
public interface ApplicationSystemResource {

	/**
	 * Check if an application token is valid.
	 *
	 * @param token The token to check.
	 * @return True if the token is valid, false otherwise.
	 */
	@GET
	@Path("/v1/system/is-token-valid")
	boolean isTokenValid(@QueryParam("token") String token);
}
