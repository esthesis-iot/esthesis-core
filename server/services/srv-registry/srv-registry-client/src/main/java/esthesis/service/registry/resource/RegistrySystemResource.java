package esthesis.service.registry.resource;

import esthesis.common.AppConstants;
import esthesis.service.registry.dto.RegistryEntry;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "RegistrySystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface RegistrySystemResource {

  @GET
  @Path("/v1/registry-system/find/by-name/{name}")
  RegistryEntry findByName(@PathParam("name") AppConstants.Registry name);

}
