package esthesis.services.security.impl.service;

import static esthesis.core.common.AppConstants.Security.Category.SECURITY;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.security.entity.RoleEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@ApplicationScoped
public class SecurityRoleService extends BaseService<RoleEntity> {

	private RoleEntity saveHandler(RoleEntity entity) {
		return super.save(entity);
	}

	@Override
	public Page<RoleEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}

	@Override
	public RoleEntity findById(String id) {
		return super.findById(id);
	}

	@Override
	@ErnPermission(category = SECURITY, operation = DELETE)
	public boolean deleteById(String deviceId) {
		return super.deleteById(deviceId);
	}

	@ErnPermission(category = SECURITY, operation = CREATE)
	public RoleEntity saveNew(RoleEntity entity) {
		return saveHandler(entity);
	}

	@ErnPermission(category = SECURITY, operation = WRITE)
	public RoleEntity saveUpdate(RoleEntity entity) {
		return saveHandler(entity);
	}

	@Override
	@ErnPermission(category = SECURITY, operation = READ)
	public long countAll() {
		return super.countAll();
	}
}
