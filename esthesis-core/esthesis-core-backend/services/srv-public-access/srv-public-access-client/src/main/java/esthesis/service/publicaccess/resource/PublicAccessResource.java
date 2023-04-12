package esthesis.service.publicaccess.resource;

import esthesis.service.publicaccess.dto.OidcConfigDTO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Settings which can be accessed by unauthenticated users.
 */
@Path("/api")
public interface PublicAccessResource {

  @GET
  @Path("/oidc-config")
  OidcConfigDTO getOidcConfig();
}
