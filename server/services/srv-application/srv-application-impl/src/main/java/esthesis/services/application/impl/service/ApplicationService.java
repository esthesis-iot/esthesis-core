package esthesis.services.application.impl.service;

import esthesis.service.application.dto.Application;
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
public class ApplicationService extends BaseService<Application> {

  @Inject
  JsonWebToken jwt;

  @Inject
  @CacheName("dt-token-is-valid")
  Cache dtTokenIsValidCache;

  @Override
  public Page<Application> find(Pageable pageable) {
    log.debug("Finding all applications with '{}'.", pageable);
    return super.find(pageable);
  }

  @Override
  public Page<Application> find(Pageable pageable, boolean partialMatch) {
    log.debug("Finding all applications with partial match with '{}'.",
        pageable);
    return super.find(pageable, partialMatch);
  }

  @Override
  public Application save(Application dto) {
    log.debug("Saving application '{}'.", dto);
    // Ensure no other application has the same name.
    Application existingApplication = findByColumn("name", dto.getName());
    if (existingApplication != null && (dto.getId() == null
        || !existingApplication.getId().equals(dto.getId()))) {
      new CVException<Application>()
          .addViolation("name", "An application with name '{}' already "
              + "exists.", dto.getName())
          .throwCVE();
    }

    // Invalidate cache.
    if (existingApplication != null) {
      dtTokenIsValidCache.invalidate(existingApplication.getToken()).await()
          .indefinitely();
    }
    dtTokenIsValidCache.invalidate(dto.getToken()).await().indefinitely();

    return super.save(dto);
  }

  @Override
  public void deleteById(ObjectId id) {
    log.debug("Deleting application with id '{}'.", id);
    Application application = findById(id);
    if (application != null) {
      super.deleteById(id);
    } else {
      log.warn("Application with id '{}' not found to be deleted.", id);
    }
  }

  @CacheResult(cacheName = "dt-token-is-valid")
  public boolean isTokenValid(String token) {
    return getRepository().find("token = ?1 and state = ?2", token, true)
        .firstResultOptional().isPresent();
  }
}
