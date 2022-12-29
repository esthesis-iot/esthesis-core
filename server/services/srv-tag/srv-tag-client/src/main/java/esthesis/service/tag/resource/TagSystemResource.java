package esthesis.service.tag.resource;

import esthesis.service.tag.entity.TagEntity;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "TagSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface TagSystemResource {

  @GET
  @Path("/v1/system/get-all")
  List<TagEntity> getAll();

  /**
   * Finds a tag by its name.
   *
   * @param name The name of the tag to search.
   */
  @GET
  @Path("/v1/system/find/by-name/{name}")
  TagEntity findByName(@PathParam("name") String name);

}
