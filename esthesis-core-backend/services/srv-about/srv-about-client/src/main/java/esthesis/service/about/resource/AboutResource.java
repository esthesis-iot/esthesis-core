package esthesis.service.about.resource;

import esthesis.service.about.dto.AboutGeneralDTO;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the About service.
 */
@Path("/api")
@AccessToken
@RegisterRestClient(configKey = "AboutResource")
public interface AboutResource {

	/**
	 * Retrieves general information about the application.
	 *
	 * @return the general information
	 */
	@GET
	@Path("/v1/general")
	AboutGeneralDTO getGeneralInfo();

}
