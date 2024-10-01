package esthesis.services.audit.impl.service;

import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.AUDIT;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;

import esthesis.common.exception.QAlreadyExistsException;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.security.annotation.ErnPermission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@ApplicationScoped
public class AuditService extends BaseService<AuditEntity> {

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = AUDIT, operation = READ)
	public Page<AuditEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = AUDIT, operation = READ)
	public AuditEntity findById(String id) {
		return super.findById(id);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = AUDIT, operation = DELETE)
	public boolean deleteById(String deviceId) {
		return super.deleteById(deviceId);
	}

	@Override
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = AUDIT, operation = CREATE)
	public AuditEntity save(AuditEntity auditEntity) {
		if (super.findById(auditEntity.getId()) != null) {
			throw new QAlreadyExistsException("Audit entries can not be updated.");
		}

		return super.save(auditEntity);
	}
}
