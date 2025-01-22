package esthesis.services.audit.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.services.audit.impl.service.AuditService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class AuditSystemResourceImpl implements AuditSystemResource {

	private final AuditService auditService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public Page<AuditEntity> find(int entries) {
		Pageable pageable = new Pageable().setPage(0).setSize(entries).setSort("createdOn,desc");

		return auditService.find(pageable, false);
	}

	@Override
	public Long countAll() {
		return auditService.countAll();
	}
}
