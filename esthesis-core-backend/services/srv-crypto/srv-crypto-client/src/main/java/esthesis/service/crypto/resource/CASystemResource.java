package esthesis.service.crypto.resource;

import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the CA System resource, when used by a SYSTEM OIDC client.
 */
@OidcClientFilter
@Path("/api/system-ca")
@RegisterRestClient(configKey = "CASystemResource")
@ApplicationScoped
public interface CASystemResource {

	/**
	 * Get the certificate of the CA with the given ID.
	 *
	 * @param caId The ID of the CA.
	 * @return The certificate of the CA.
	 */
	@GET
	@Path("/v1/{caId}/certificate")
	String getCACertificate(@PathParam("caId") String caId);
}
