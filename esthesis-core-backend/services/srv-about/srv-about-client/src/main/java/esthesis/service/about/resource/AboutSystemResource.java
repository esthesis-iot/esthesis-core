package esthesis.service.about.resource;

import esthesis.service.about.dto.AboutGeneralDTO;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "AboutSystemResource")
public interface AboutSystemResource {

	@GET
	@Path("/v1/system/general")
	AboutGeneralDTO getGeneralInfo();

}
