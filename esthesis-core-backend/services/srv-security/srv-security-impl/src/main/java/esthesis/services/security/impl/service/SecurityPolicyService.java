package esthesis.services.security.impl.service;

import static esthesis.core.common.AppConstants.Security.Category.SECURITY;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.security.entity.PolicyEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@ApplicationScoped
public class SecurityPolicyService extends BaseService<PolicyEntity> {

	private PolicyEntity saveHandler(PolicyEntity policyEntity) {
		return super.save(policyEntity);
	}

	@Override
	public Page<PolicyEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}

	@Override
	public PolicyEntity findById(String id) {
		return super.findById(id);
	}

	@Override
	@ErnPermission(category = SECURITY, operation = DELETE)
	public boolean deleteById(String deviceId) {
		return super.deleteById(deviceId);
	}

	@ErnPermission(category = SECURITY, operation = CREATE)
	public PolicyEntity saveNew(PolicyEntity policyEntity) {
		return saveHandler(policyEntity);
	}

	@ErnPermission(category = SECURITY, operation = WRITE)
	public PolicyEntity saveUpdate(PolicyEntity policyEntity) {
		return saveHandler(policyEntity);
	}
}
