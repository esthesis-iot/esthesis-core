package esthesis.service.crypto.resource;

import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@OidcClientFilter
@Path("/api/system-ca")
@RegisterRestClient(configKey = "CASystemResource")
@ApplicationScoped
public interface CASystemResource {

	@GET
	@Path("/v1/{caId}/certificate")
	String getCACertificate(@PathParam("caId") String caId);
}
