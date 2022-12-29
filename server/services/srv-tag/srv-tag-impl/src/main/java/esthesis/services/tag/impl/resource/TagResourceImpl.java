package esthesis.services.tag.impl.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.tag.impl.service.TagService;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class TagResourceImpl implements TagResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  TagService tagService;

  @GET
  @Override
  @Path("/v1/find")
  public Page<TagEntity> find(@BeanParam Pageable pageable) {
    return tagService.find(pageable, true);
  }

  @Override
  public List<TagEntity> getAll() {
    return tagService.getAll();
  }

  @Override
  public TagEntity findById(@PathParam("id") ObjectId id) {
    return tagService.findById(id);
  }

  @Override
  public TagEntity findByName(@PathParam("name") String name, boolean partialMatch) {
    return tagService.findFirstByColumn("name", name, partialMatch);
  }

  @Override
  public List<TagEntity> findByNames(@QueryParam("names") String names,
      boolean partialMatch) {
    return tagService.findByColumnIn("name", Arrays.asList(names.split(",")),
        partialMatch);
  }

  @Override
  public Response delete(@PathParam("id") ObjectId id) {
    return tagService.deleteById(id) ? Response.ok().build() : Response.notModified().build();
  }

  @Override
  public TagEntity save(@Valid TagEntity tagEntity) {
    return tagService.save(tagEntity);
  }
}
