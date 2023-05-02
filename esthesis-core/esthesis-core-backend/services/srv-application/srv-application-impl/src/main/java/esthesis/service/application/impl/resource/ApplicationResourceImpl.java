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
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class ApplicationResourceImpl implements ApplicationResource {

	@Inject
	JsonWebToken jwt;

	@Inject
	ApplicationService applicationService;

	@GET
	@Override
	@Path("/v1/find")
	@Audited(cat = Category.APPLICATION, op = Operation.READ, msg = "Search applications",
		log = AuditLogType.DATA_IN)
	public Page<ApplicationEntity> find(@BeanParam Pageable pageable) {
		return applicationService.find(pageable, true);
	}

	@Override
	@Audited(cat = Category.APPLICATION, op = Operation.READ, msg = "View application")
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
	@Audited(cat = Category.APPLICATION, op = Operation.WRITE, msg = "Save application")
	public ApplicationEntity save(@Valid ApplicationEntity tag) {
		return applicationService.save(tag);
	}
}
