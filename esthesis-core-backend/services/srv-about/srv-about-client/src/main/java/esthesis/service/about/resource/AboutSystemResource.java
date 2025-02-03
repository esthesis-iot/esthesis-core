package esthesis.service.about.resource;

import esthesis.service.about.dto.AboutGeneralDTO;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the About service, when the service is secured with a system OIDC account.
 */
@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "AboutSystemResource")
public interface AboutSystemResource {

	/**
	 * Retrieves general information about the application.
	 *
	 * @return the general information
	 */
	@GET
	@Path("/v1/system/general")
	AboutGeneralDTO getGeneralInfo();

}
