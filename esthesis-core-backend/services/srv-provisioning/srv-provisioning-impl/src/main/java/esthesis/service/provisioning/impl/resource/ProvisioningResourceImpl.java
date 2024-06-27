package esthesis.service.provisioning.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import esthesis.service.provisioning.impl.service.ProvisioningService;
import esthesis.service.provisioning.resource.ProvisioningResource;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

public class ProvisioningResourceImpl implements ProvisioningResource {

	@Inject
	ProvisioningService provisioningService;

	@GET
	@Override
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.PROVISIONING, op = Operation.READ, msg = "Search provisioning packages"
		, log = AuditLogType.DATA_IN)
	public Page<ProvisioningPackageEntity> find(@BeanParam Pageable pageable) {
		return provisioningService.find(pageable);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.PROVISIONING, op = Operation.READ, msg = "View provisioning package")
	public ProvisioningPackageEntity findById(String id) {
		return provisioningService.findById(id);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.PROVISIONING, op = Operation.WRITE, msg = "Save provisioning package")
	public ProvisioningPackageEntity save(ProvisioningPackageForm pf) {
		return provisioningService.save(pf);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.PROVISIONING, op = Operation.DELETE, msg = "Delete provisioning package")
	public void delete(String provisioningPackageId) {
		provisioningService.delete(provisioningPackageId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.PROVISIONING, op = Operation.READ, msg = "Download provisioning "
		+ "package", log = AuditLogType.DATA_IN)
	public Uni<RestResponse<byte[]>> download(String provisioningPackageId) {
		ProvisioningPackageEntity pp = provisioningService.findById(provisioningPackageId);
		Uni<byte[]> binary = provisioningService.download(provisioningPackageId);

		return binary.onItem().transform(b -> ResponseBuilder.ok(b)
			.header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + pp.getFilename() + "\"")
			.header(HttpHeaders.CONTENT_LENGTH, pp.getSize())
			.build());
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<ProvisioningPackageEntity> findByTags(String tags) {
		return provisioningService.findByTags(tags);
	}

}
