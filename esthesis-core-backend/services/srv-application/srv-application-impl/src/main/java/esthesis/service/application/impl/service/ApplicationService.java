package esthesis.service.application.impl.service;

import static esthesis.core.common.AppConstants.Security.Category.APPLICATION;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.service.application.entity.ApplicationEntity;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.security.annotation.ErnPermission;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides functionality to manage applications.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class ApplicationService extends BaseService<ApplicationEntity> {

	@Inject
	@CacheName("dt-token-is-valid")
	Cache dtTokenIsValidCache;

	/**
	 * Invalidate the cache for the token of the application entity.
	 *
	 * @param applicationEntity the application entity to invalidate the cache for.
	 */
	private void invalidateCache(ApplicationEntity applicationEntity) {
		ApplicationEntity existingApplicationEntity = findFirstByColumn("name",
			applicationEntity.getName());
		if (existingApplicationEntity != null) {
			dtTokenIsValidCache.invalidate(existingApplicationEntity.getToken()).await()
				.indefinitely();
		} else {
			dtTokenIsValidCache.invalidate(applicationEntity.getToken()).await().indefinitely();
		}
	}

	/**
	 * Save handler for the application entity. This is a grouping function to allow to have different
	 * security permissions for 'create new', and 'edit existing' records.
	 *
	 * @param applicationEntity the application entity to save.
	 * @return the saved application entity.
	 */
	private ApplicationEntity saveHandler(ApplicationEntity applicationEntity) {
		invalidateCache(applicationEntity);
		applicationEntity.setCreatedOn(Instant.now());

		return super.save(applicationEntity);
	}

	/**
	 * Find all applications.
	 *
	 * @param pageable a pageable object to define the page and size of the result.
	 * @return a page of application entities.
	 */
	@Override
	@ErnPermission(category = APPLICATION, operation = READ)
	public Page<ApplicationEntity> find(Pageable pageable) {
		log.debug("Finding all applications with '{}'.", pageable);
		return super.find(pageable);
	}
	
	/**
	 * Save a new application entity.
	 *
	 * @param applicationEntity the application entity to save.
	 * @return the saved application entity.
	 */
	@ErnPermission(category = APPLICATION, operation = CREATE)
	public ApplicationEntity saveNew(ApplicationEntity applicationEntity) {
		return saveHandler(applicationEntity);
	}

	/**
	 * Updates an existing application entity.
	 *
	 * @param applicationEntity the application entity to save.
	 * @return the saved application entity.
	 */
	@ErnPermission(category = APPLICATION, operation = WRITE)
	public ApplicationEntity saveUpdate(ApplicationEntity applicationEntity) {
		return saveHandler(applicationEntity);
	}

	/**
	 * Deletes an application entity by id.
	 *
	 * @param id the id of the application entity to delete.
	 * @return true if the application entity was deleted, false otherwise.
	 */
	@Override
	@ErnPermission(category = APPLICATION, operation = DELETE)
	public boolean deleteById(String id) {
		log.debug("Deleting application with id '{}'.", id);
		ApplicationEntity applicationEntity = findById(id);
		if (applicationEntity != null) {
			dtTokenIsValidCache.invalidate(applicationEntity.getToken()).await()
				.indefinitely();
			return super.deleteById(id);
		} else {
			log.warn("Application with id '{}' not found to be deleted.", id);
			return false;
		}
	}

	/**
	 * Find an application entity by ID.
	 *
	 * @param id the id of the application entity to find.
	 * @return the application entity if found, null otherwise.
	 */
	@Override
	@ErnPermission(category = APPLICATION, operation = READ)
	public ApplicationEntity findById(String id) {
		return super.findById(id);
	}

	/**
	 * Check if an application token is valid.
	 *
	 * @param token the token to check.
	 * @return true if the token is valid, false otherwise.
	 */
	@CacheResult(cacheName = "dt-token-is-valid")
	public boolean isTokenValid(String token) {
		return getRepository().find("token = ?1 and state = ?2", token, true)
			.firstResultOptional().isPresent();
	}
}
