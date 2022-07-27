package esthesis.service.registry.resource;

import esthesis.service.registry.dto.RegistryEntry;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/registry")
@RegisterRestClient(configKey = "RegistryResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface RegistryResourceV1 {

  @GET
  @Path("/{id}")
  RegistryEntry findById(@PathParam("id") ObjectId id);

  @GET
  @Path("/find/by-name/{name}")
  RegistryEntry findByName(@PathParam("name") String name);
}
