package esthesis.services.audit.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.audit.resource.AuditResource;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.services.audit.impl.service.AuditService;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class AuditResourceImpl implements AuditResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  AuditService auditService;

  @GET
  @Override
  @Path("/v1/find")
  @Audited(cat = Category.AUDIT, op = Operation.RETRIEVE, msg = "Search audit",
      log = AuditLogType.DATA_IN)
  @JSONReplyFilter(filter = "content,content.id,content.createdOn,content.createdBy,"
      + "content.operation,content.category,content.message")
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
  @Audited(cat = Category.AUDIT, op = Operation.RETRIEVE, msg = "View audit entry")
  public AuditEntity findById(String id) {
    return auditService.findById(id);
  }

  @Override
  @Audited(cat = Category.AUDIT, op = Operation.RETRIEVE, msg = "Delete audit entry")
  public Response delete(String id) {
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
