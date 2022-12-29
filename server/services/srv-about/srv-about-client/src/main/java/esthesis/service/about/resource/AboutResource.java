package esthesis.service.about.resource;

import esthesis.service.about.dto.AboutGeneralDTO;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "AboutResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface AboutResource {

  @GET
  @Path("/v1/general")
  AboutGeneralDTO getGeneralInfo();

}
