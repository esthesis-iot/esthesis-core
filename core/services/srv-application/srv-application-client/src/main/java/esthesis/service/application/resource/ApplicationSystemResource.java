package esthesis.service.application.resource;

import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "ApplicationSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface ApplicationSystemResource {

  @GET
  @Path("/v1/system/is-token-valid")
  boolean isTokenValid(@QueryParam("token") String token);

}
