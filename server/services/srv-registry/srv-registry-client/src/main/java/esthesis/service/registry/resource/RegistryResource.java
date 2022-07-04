package esthesis.service.registry.resource;

import esthesis.service.registry.dto.RegistryEntry;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/registry")
@RegisterRestClient(configKey = "RegistryResource")
public interface RegistryResource {

  @GET
  @Path("/{id}")
  RegistryEntry findById(@PathParam("id") ObjectId id);

  @GET
  @Path("/find/by-name/{name}")
  RegistryEntry findByName(@PathParam("name") String name);

}
