package esthesis.services.audit.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.audit.resource.AuditResource;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.services.audit.impl.service.AuditService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
public class AuditResourceImpl implements AuditResource {

	@Inject
	AuditService auditService;

	@GET
	@Override
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.AUDIT, op = Operation.READ, msg = "Search audit",
		log = AuditLogType.DATA_IN)
	@JSONReplyFilter(filter = "content,content.id,content.createdOn,content.createdBy,"
		+ "content.operation,content.category,content.message")
	public Page<AuditEntity> find(@BeanParam Pageable pageable) {
		return auditService.find(pageable);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public Category[] getCategories() {
		return Security.Category.values();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public Operation[] getOperations() {
		return Operation.values();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.AUDIT, op = Operation.READ, msg = "View audit entry")
	public AuditEntity findById(String id) {
		return auditService.findById(id);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.AUDIT, op = Operation.READ, msg = "Delete audit entry")
	public Response delete(String id) {
		if (auditService.deleteById(id)) {
			return Response.ok().build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public AuditEntity save(AuditEntity auditEntity) {
		return auditService.save(auditEntity);
	}
}
