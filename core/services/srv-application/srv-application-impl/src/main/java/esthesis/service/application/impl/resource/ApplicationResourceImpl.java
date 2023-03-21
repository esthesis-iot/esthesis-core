package esthesis.service.application.impl.resource;

import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.application.entity.ApplicationEntity;
import esthesis.service.application.impl.service.ApplicationService;
import esthesis.service.application.resource.ApplicationResource;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class ApplicationResourceImpl implements ApplicationResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  ApplicationService applicationService;

  @GET
  @Override
  @Path("/v1/find")
  @Audited(cat = Category.APPLICATION, op = Operation.RETRIEVE, msg = "Search applications",
      log = AuditLogType.DATA_IN)
  public Page<ApplicationEntity> find(@BeanParam Pageable pageable) {
    return applicationService.find(pageable, true);
  }

  @Override
  @Audited(cat = Category.APPLICATION, op = Operation.RETRIEVE, msg = "View application")
  public ApplicationEntity findById(@PathParam("id") String id) {
    return applicationService.findById(id);
  }

  @Override
  @Audited(cat = Category.APPLICATION, op = Operation.DELETE, msg = "Delete applications")
  public Response delete(@PathParam("id") String id) {
    applicationService.deleteById(id);

    return Response.ok().build();
  }

  @Override
  @Audited(cat = Category.APPLICATION, op = Operation.UPDATE, msg = "Save application")
  public ApplicationEntity save(@Valid ApplicationEntity tag) {
    return applicationService.save(tag);
  }
}
