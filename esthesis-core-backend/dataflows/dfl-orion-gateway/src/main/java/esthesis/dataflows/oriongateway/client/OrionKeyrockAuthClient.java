package esthesis.dataflows.oriongateway.client;

import esthesis.dataflow.common.RestClientExceptionMapper;
import esthesis.dataflows.oriongateway.dto.OrionKeyrockAccessTokenDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * A REST client to request access token from Keyrock.
 */
@Path("")
@Produces("application/json")
@RegisterRestClient(configKey = "KeyrockAuthClient")
@RegisterProvider(RestClientExceptionMapper.class)
public interface OrionKeyrockAuthClient {

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/oauth2/token")
	@Retry(maxRetries = 3)
	OrionKeyrockAccessTokenDTO requestAccessToken(@FormParam("username") String username,
		@FormParam("password") String password,
		@FormParam("grant_type") String grantType,
		@HeaderParam("Authorization") String basicAuth);

}
