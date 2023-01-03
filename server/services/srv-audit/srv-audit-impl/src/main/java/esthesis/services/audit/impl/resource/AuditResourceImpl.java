package esthesis.services.audit.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.audit.resource.AuditResource;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.services.audit.impl.service.AuditService;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class AuditResourceImpl implements AuditResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  AuditService auditService;

  @GET
  @Override
  @Path("/v1/find")
  public Page<AuditEntity> find(@BeanParam Pageable pageable) {
    return auditService.find(pageable);
  }

  @Override
  public Category[] getCategories() {
    return AppConstants.Audit.Category.values();
  }

  @Override
  public Operation[] getOperations() {
    return Operation.values();
  }

  @Override
  public AuditEntity findById(ObjectId id) {
    return auditService.findById(id);
  }

  @Override
  public Response delete(ObjectId id) {
    if (auditService.deleteById(id)) {
      return Response.ok().build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @Override
  public AuditEntity save(AuditEntity auditEntity) {
    return auditService.save(auditEntity);
  }

}
