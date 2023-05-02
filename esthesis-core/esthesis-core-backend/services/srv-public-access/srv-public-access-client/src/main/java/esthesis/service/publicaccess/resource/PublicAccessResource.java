package esthesis.service.publicaccess.resource;

import esthesis.service.publicaccess.dto.OidcConfigDTO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * Settings which can be accessed by unauthenticated users.
 */
@Path("/api")
public interface PublicAccessResource {

	@GET
	@Path("/oidc-config")
	OidcConfigDTO getOidcConfig();
}
