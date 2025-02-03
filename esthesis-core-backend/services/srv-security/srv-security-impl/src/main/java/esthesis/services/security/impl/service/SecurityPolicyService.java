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
import esthesis.service.security.entity.PolicyEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing security policies.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class SecurityPolicyService extends BaseService<PolicyEntity> {

	/**
	 * Save handler for the security policies.
	 *
	 * @param policyEntity The security policy entity to save.
	 * @return The saved security policy.
	 */
	private PolicyEntity saveHandler(PolicyEntity policyEntity) {
		return super.save(policyEntity);
	}

	/**
	 * Find all security policies.
	 *
	 * @param pageable     Representation of page, size, and sort search parameters.
	 * @param partialMatch Whether to do a partial match.
	 * @return The page of security policies.
	 */
	@Override
	public Page<PolicyEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}

	/**
	 * Find a security policy by its ID.
	 *
	 * @param id The ID of the entity to find.
	 * @return The security policy entity.
	 */
	@Override
	public PolicyEntity findById(String id) {
		return super.findById(id);
	}

	/**
	 * Delete a security policy by its ID.
	 *
	 * @param deviceId The ID of the entity to delete.
	 * @return Whether the deletion was successful.
	 */
	@Override
	@ErnPermission(category = SECURITY, operation = DELETE)
	public boolean deleteById(String deviceId) {
		return super.deleteById(deviceId);
	}

	/**
	 * Create a new security policy.
	 *
	 * @param policyEntity The security policy entity to create.
	 * @return The created security policy.
	 */
	@ErnPermission(category = SECURITY, operation = CREATE)
	public PolicyEntity saveNew(PolicyEntity policyEntity) {
		return saveHandler(policyEntity);
	}

	/**
	 * Update an existing security policy.
	 *
	 * @param policyEntity The security policy entity to update.
	 * @return The updated security policy.
	 */
	@ErnPermission(category = SECURITY, operation = WRITE)
	public PolicyEntity saveUpdate(PolicyEntity policyEntity) {
		return saveHandler(policyEntity);
	}

	/**
	 * Count all security policies.
	 *
	 * @return The number of security policies.
	 */
	@Override
	@ErnPermission(category = SECURITY, operation = READ)
	public long countAll() {
		return super.countAll();
	}
}
