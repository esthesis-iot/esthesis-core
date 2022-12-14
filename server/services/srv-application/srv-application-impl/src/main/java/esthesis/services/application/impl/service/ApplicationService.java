package esthesis.services.application.impl.service;

import esthesis.service.application.entity.ApplicationEntity;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.common.validation.CVException;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CacheResult;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@ApplicationScoped
public class ApplicationService extends BaseService<ApplicationEntity> {

  @Inject
  JsonWebToken jwt;

  @Inject
  @CacheName("dt-token-is-valid")
  Cache dtTokenIsValidCache;

  @Override
  public Page<ApplicationEntity> find(Pageable pageable) {
    log.debug("Finding all applications with '{}'.", pageable);
    return super.find(pageable);
  }

  @Override
  public Page<ApplicationEntity> find(Pageable pageable, boolean partialMatch) {
    log.debug("Finding all applications with partial match with '{}'.",
        pageable);
    return super.find(pageable, partialMatch);
  }

  @Override
  public ApplicationEntity save(ApplicationEntity dto) {
    log.debug("Saving application '{}'.", dto);
    // Ensure no other application has the same name.
    ApplicationEntity existingApplicationEntity = findFirstByColumn("name", dto.getName());
    if (existingApplicationEntity != null && (dto.getId() == null
        || !existingApplicationEntity.getId().equals(dto.getId()))) {
      new CVException<ApplicationEntity>()
          .addViolation("name", "An application with name '{}' already "
              + "exists.", dto.getName())
          .throwCVE();
    }

    // Invalidate cache.
    if (existingApplicationEntity != null) {
      dtTokenIsValidCache.invalidate(existingApplicationEntity.getToken()).await()
          .indefinitely();
    }
    dtTokenIsValidCache.invalidate(dto.getToken()).await().indefinitely();

    return super.save(dto);
  }

  @Override
  public boolean deleteById(ObjectId id) {
    log.debug("Deleting application with id '{}'.", id);
    ApplicationEntity applicationEntity = findById(id);
    if (applicationEntity != null) {
      return super.deleteById(id);
    } else {
      log.warn("Application with id '{}' not found to be deleted.", id);
      return false;
    }
  }

  @CacheResult(cacheName = "dt-token-is-valid")
  public boolean isTokenValid(String token) {
    return getRepository().find("token = ?1 and state = ?2", token, true)
        .firstResultOptional().isPresent();
  }
}
