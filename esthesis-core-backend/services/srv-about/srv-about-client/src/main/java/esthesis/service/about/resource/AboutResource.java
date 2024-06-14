package esthesis.service.about.resource;

import esthesis.service.about.dto.AboutGeneralDTO;
import io.quarkus.oidc.token.propagation.AccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "AboutResource")
@AccessToken
public interface AboutResource {

	@GET
	@Path("/v1/general")
	AboutGeneralDTO getGeneralInfo();

}
