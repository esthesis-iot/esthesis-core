package esthesis.service.publicaccess.resource;

import esthesis.service.publicaccess.dto.OidcConfigDTO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * Information that can be accessed by unauthenticated users.
 */
@Path("/api")
public interface PublicAccessResource {

	/**
	 * Gets the OIDC configuration.
	 *
	 * @return the OIDC configuration
	 */
	@GET
	@Path("/oidc-config")
	OidcConfigDTO getOidcConfig();
}
