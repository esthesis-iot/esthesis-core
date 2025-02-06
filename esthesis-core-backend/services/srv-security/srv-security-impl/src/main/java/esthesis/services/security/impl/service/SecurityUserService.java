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
import esthesis.service.security.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Service class for managing security users.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class SecurityUserService extends BaseService<UserEntity> {

	@ConfigProperty(name = "esthesis.security.admin.username")
	String esthesisAdminUsername;

	@ConfigProperty(name = "esthesis.security.defaultAdminGroupId")
	String defaultAdminGroupId;

	/**
	 * Save handler for the security users.
	 *
	 * @param userEntity The security user entity to save.
	 * @return The saved security user.
	 */
	private UserEntity saveHandler(UserEntity userEntity) {
		return super.save(userEntity);
	}

	/**
	 * Find all security users.
	 *
	 * @param username The username of the entity to find.
	 * @return The security user entity.
	 */
	public UserEntity findByUsername(String username) {
		return findFirstByColumn("username", username);
	}

	/**
	 * If User collection is empty, create a default admin user.
	 */
	public void createDefaultAdmin() {
		//TODO review this method, is this logic still needed? Could we inject this logic in
		// liquibase scripts or similar?
		if (getRepository().count() == 0) {
			log.info("No administrators found. Creating default admin user with username '{}'.",
				esthesisAdminUsername);
			UserEntity user = new UserEntity();
			user.setUsername(esthesisAdminUsername);
			user.setDescription("Default system-created admin account.");
			user.setFirstName("esthesis");
			user.setLastName("admin");
			user.setGroups(List.of(defaultAdminGroupId));
			save(user);
			log.info("Default admin user created.");
		}
	}

	/**
	 * Save a new security user.
	 *
	 * @param userEntity The security user entity to save.
	 * @return The saved security user.
	 */
	@ErnPermission(category = SECURITY, operation = CREATE)
	public UserEntity saveNew(UserEntity userEntity) {
		return saveHandler(userEntity);
	}

	/**
	 * Save an updated security user.
	 *
	 * @param userEntity The security user entity to save.
	 * @return The saved security user.
	 */
	@ErnPermission(category = SECURITY, operation = WRITE)
	public UserEntity saveUpdate(UserEntity userEntity) {
		return saveHandler(userEntity);
	}

	/**
	 * Delete a security user by its ID.
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
	 * Find a security user by its ID.
	 *
	 * @param id The ID of the entity to find.
	 * @return The security user entity.
	 */
	@Override
	public UserEntity findById(String id) {
		return super.findById(id);
	}

	/**
	 * Find all security users.
	 *
	 * @param pageable Representation of page, size, and sort search parameters.
	 * @return The page of security users.
	 */
	@Override
	public Page<UserEntity> find(Pageable pageable) {
		return super.find(pageable);
	}

	/**
	 * Count all security users.
	 *
	 * @return The number of security users.
	 */
	@Override
	@ErnPermission(category = SECURITY, operation = READ)
	public long countAll() {
		return super.countAll();
	}
}
