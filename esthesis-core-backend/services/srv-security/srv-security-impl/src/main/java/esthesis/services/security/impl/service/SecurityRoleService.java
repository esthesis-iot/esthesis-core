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

/**
 * Service class for managing security roles.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class SecurityRoleService extends BaseService<RoleEntity> {

	/**
	 * Save handler for the security roles.
	 *
	 * @param entity The security role entity to save.
	 * @return The saved security role.
	 */
	private RoleEntity saveHandler(RoleEntity entity) {
		return super.save(entity);
	}

	/**
	 * Find all security roles.
	 *
	 * @param pageable Representation of page, size, and sort search parameters.
	 * @return The page of security roles.
	 */
	@Override
	public Page<RoleEntity> find(Pageable pageable) {
		return super.find(pageable);
	}

	/**
	 * Find a security role by its ID.
	 *
	 * @param id The ID of the entity to find.
	 * @return The security role entity.
	 */
	@Override
	public RoleEntity findById(String id) {
		return super.findById(id);
	}

	/**
	 * Delete a security role by its ID.
	 *
	 * @param deviceId The ID of the entity to delete.
	 * @return Whether the entity was deleted.
	 */
	@Override
	@ErnPermission(category = SECURITY, operation = DELETE)
	public boolean deleteById(String deviceId) {
		return super.deleteById(deviceId);
	}

	/**
	 * Save a new security role.
	 *
	 * @param entity The security role entity to save.
	 * @return The saved security role.
	 */
	@ErnPermission(category = SECURITY, operation = CREATE)
	public RoleEntity saveNew(RoleEntity entity) {
		return saveHandler(entity);
	}

	/**
	 * Save an updated security role.
	 *
	 * @param entity The security role entity to save.
	 * @return The saved security role.
	 */
	@ErnPermission(category = SECURITY, operation = WRITE)
	public RoleEntity saveUpdate(RoleEntity entity) {
		return saveHandler(entity);
	}

	/**
	 * Count all security roles.
	 *
	 * @return The number of security roles.
	 */
	@Override
	@ErnPermission(category = SECURITY, operation = READ)
	public long countAll() {
		return super.countAll();
	}
}
