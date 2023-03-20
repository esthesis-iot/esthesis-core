package esthesis.services.tag.impl.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
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
import org.eclipse.microprofile.jwt.JsonWebToken;

public class TagResourceImpl implements TagResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  TagService tagService;

  @GET
  @Override
  @Path("/v1/find")
  @Audited(cat = Category.TAG, op = Operation.RETRIEVE, msg = "Search tags", log = AuditLogType.DATA_IN)
  public Page<TagEntity> find(@BeanParam Pageable pageable) {
    return tagService.find(pageable, true);
  }

  @Override
  public List<TagEntity> getAll() {
    return tagService.getAll();
  }

  @Override
  @Audited(cat = Category.TAG, op = Operation.RETRIEVE, msg = "View tag")
  public TagEntity findById(@PathParam("id") String id) {
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
  @Audited(cat = Category.TAG, op = Operation.DELETE, msg = "Delete tag")
  public Response delete(@PathParam("id") String id) {
    return tagService.deleteById(id) ? Response.ok().build() : Response.notModified().build();
  }

  @Override
  @Audited(cat = Category.TAG, op = Operation.UPDATE, msg = "Save tag")
  public TagEntity save(@Valid TagEntity tagEntity) {
    return tagService.save(tagEntity);
  }
}
