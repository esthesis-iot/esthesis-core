package esthesis.services.tag.impl.resource;

import esthesis.common.rest.Page;
import esthesis.common.rest.Pageable;
import esthesis.service.tag.dto.Tag;
import esthesis.service.tag.resource.TagResourceV1;
import esthesis.services.tag.impl.service.TagService;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class TagResourceV1Impl implements TagResourceV1 {

  @Inject
  JsonWebToken jwt;

  @Inject
  TagService tagService;

  @GET
  @Override
  @Path("/find")
  public Page<Tag> find(@BeanParam Pageable pageable) {
    return tagService.find(pageable, true);
  }

  @Override
  public Tag findById(@PathParam("id") ObjectId id) {
    return tagService.findById(id);
  }

  @Override
  public Tag findByName(@PathParam("name") String name) {
    return tagService.findByColumn("name", name);
  }

  @Override
  public Response delete(@PathParam("id") ObjectId id) {
    tagService.deleteById(id);

    return Response.ok().build();
  }

  @Override
  public Tag save(@Valid Tag tag) {
    return tagService.save(tag);
  }
}
