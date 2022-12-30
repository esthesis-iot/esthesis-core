package esthesis.service.application.impl.resource;

import esthesis.service.application.entity.ApplicationEntity;
import esthesis.service.application.impl.service.ApplicationService;
import esthesis.service.application.resource.ApplicationResource;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class ApplicationResourceImpl implements ApplicationResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  ApplicationService applicationService;

  @GET
  @Override
  @Path("/v1/find")
  public Page<ApplicationEntity> find(@BeanParam Pageable pageable) {
    return applicationService.find(pageable, true);
  }

  @Override
  public ApplicationEntity findById(@PathParam("id") ObjectId id) {
    return applicationService.findById(id);
  }

  @Override
  public Response delete(@PathParam("id") ObjectId id) {
    applicationService.deleteById(id);

    return Response.ok().build();
  }

  @Override
  public ApplicationEntity save(@Valid ApplicationEntity tag) {
    return applicationService.save(tag);
  }
}
