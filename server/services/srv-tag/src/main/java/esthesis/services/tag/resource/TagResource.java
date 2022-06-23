package esthesis.services.tag.resource;

import esthesis.dto.Tag;
import esthesis.resource.Pageable;
import esthesis.service.Page;
import esthesis.services.tag.service.TagService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.bson.types.ObjectId;

@Path("/api/v1/tag")
@RequestScoped
public class TagResource {

  @Inject
  TagService tagService;

  @GET
  @Produces("application/json")
  public Page<Tag> find(@BeanParam Pageable pageable) {
    return tagService.find(pageable, true);
  }

  @GET
  @Path("/{id}")
  @Produces("application/json")
  public Tag findById(@PathParam("id") ObjectId id) {
    return tagService.findById(id);
  }
}
