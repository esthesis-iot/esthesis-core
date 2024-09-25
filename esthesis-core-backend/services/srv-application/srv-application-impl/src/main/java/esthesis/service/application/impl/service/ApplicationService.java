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

@Slf4j
@Transactional
@ApplicationScoped
public class ApplicationService extends BaseService<ApplicationEntity> {

	@Inject
	@CacheName("dt-token-is-valid")
	Cache dtTokenIsValidCache;

	/**
	 * Invalidate the cache for the token of the application entity.
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

	private ApplicationEntity saveHandler(ApplicationEntity applicationEntity) {
		invalidateCache(applicationEntity);
		applicationEntity.setCreatedOn(Instant.now());

		return super.save(applicationEntity);
	}

	@Override
	@ErnPermission(category = APPLICATION, operation = READ)
	public Page<ApplicationEntity> find(Pageable pageable) {
		log.debug("Finding all applications with '{}'.", pageable);
		return super.find(pageable);
	}

	@Override
	@ErnPermission(category = APPLICATION, operation = READ)
	public Page<ApplicationEntity> find(Pageable pageable, boolean partialMatch) {
		log.debug("Finding all applications with partial match with '{}'.",
			pageable);
		return super.find(pageable, partialMatch);
	}

	@ErnPermission(category = APPLICATION, operation = CREATE)
	public ApplicationEntity saveNew(ApplicationEntity applicationEntity) {
		return saveHandler(applicationEntity);
	}

	@ErnPermission(category = APPLICATION, operation = WRITE)
	public ApplicationEntity saveUpdate(ApplicationEntity applicationEntity) {
		return saveHandler(applicationEntity);
	}

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

	@Override
	@ErnPermission(category = APPLICATION, operation = READ)
	public ApplicationEntity findById(String id) {
		return super.findById(id);
	}

	@CacheResult(cacheName = "dt-token-is-valid")
	public boolean isTokenValid(String token) {
		return getRepository().find("token = ?1 and state = ?2", token, true)
			.firstResultOptional().isPresent();
	}
}
