package esthesis.services.tag.resource;

import esthesis.dto.Tag;
import esthesis.services.tag.service.TagService;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@RequestScoped
@Path("/api/v1/tag")
public class TagResource {

  @Inject
  TagService tagService;

  @GET
  public List<Tag> getAll() {
    return tagService.getAll();
  }

  @POST
  public Tag save(Tag tag) {
    tagService.save(tag);
    return null;
  }
}
