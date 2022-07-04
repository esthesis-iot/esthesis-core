package esthesis.service.tag.resource;

import esthesis.common.service.rest.Page;
import esthesis.common.service.rest.Pageable;
import esthesis.service.tag.dto.Tag;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "TagResource")
public interface TagResource {

  @GET
  @Path("/find")
  @Produces("application/json")
  Page<Tag> find(@BeanParam Pageable pageable);

  @GET
  @Path("/find/by-name/{name}")
  Tag findByName(@PathParam("name") String name);

  @GET
  @Path("/{id}")
  Tag findById(@PathParam("id") ObjectId id);

  @DELETE
  @Path("/{id}")
  @Produces("application/json")
  Response delete(@PathParam("id") ObjectId id);

  @POST
  @Produces("application/json")
  Tag save(@Valid Tag tag);
}
