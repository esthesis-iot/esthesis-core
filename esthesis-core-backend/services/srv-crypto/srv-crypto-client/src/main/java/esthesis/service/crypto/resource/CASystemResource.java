package esthesis.service.crypto.resource;

import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
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
	String getCACertificate(@PathParam("caId") String caId);
}
