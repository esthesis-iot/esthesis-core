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
import esthesis.service.security.entity.GroupEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing security groups.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class SecurityGroupService extends BaseService<GroupEntity> {

	/**
	 * Save handler for groups.
	 *
	 * @param entity The entity to save.
	 * @return The saved entity.
	 */
	private GroupEntity saveHandler(GroupEntity entity) {
		return super.save(entity);
	}

	/**
	 * Find all groups.
	 *
	 * @param pageable Representation of page, size, and sort search parameters.
	 * @return The page of groups.
	 */
	@Override
	public Page<GroupEntity> find(Pageable pageable) {
		return super.find(pageable);
	}

	/**
	 * Find a group by ID.
	 *
	 * @param id The ID of the entity to find.
	 * @return The group entity.
	 */
	@Override
	public GroupEntity findById(String id) {
		return super.findById(id);
	}

	/**
	 * Delete a group by ID.
	 *
	 * @param deviceId The ID of the entity to delete.
	 * @return True if the entity was deleted, false otherwise.
	 */
	@Override
	@ErnPermission(category = SECURITY, operation = DELETE)
	public boolean deleteById(String deviceId) {
		return super.deleteById(deviceId);
	}

	/**
	 * Save a new group.
	 *
	 * @param entity The entity to save.
	 * @return The saved entity.
	 */
	@ErnPermission(category = SECURITY, operation = CREATE)
	public GroupEntity saveNew(GroupEntity entity) {
		return saveHandler(entity);
	}

	/**
	 * Update a group.
	 *
	 * @param entity The entity to update.
	 * @return The updated entity.
	 */
	@ErnPermission(category = SECURITY, operation = WRITE)
	public GroupEntity saveUpdate(GroupEntity entity) {
		return saveHandler(entity);
	}

	/**
	 * Count all groups.
	 *
	 * @return The number of groups.
	 */
	@Override
	@ErnPermission(category = SECURITY, operation = READ)
	public long countAll() {
		return super.countAll();
	}

}
