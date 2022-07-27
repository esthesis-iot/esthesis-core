package esthesis.services.registry.impl.service;

import esthesis.common.service.BaseService;
import esthesis.service.registry.dto.RegistryEntry;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@ApplicationScoped
public class RegistryService extends BaseService<RegistryEntry> {

  @Inject
  JsonWebToken jwt;

//  private static final Logger log = Logger.getLogger(RegistryService.class);

  public RegistryEntry findByName(String name) {
    log.debug("Looking up key '{}'.", name);
    RegistryEntry registryEntry = findByColumn("name", name);
    log.debug("Found value '{}'.", registryEntry);

    return registryEntry;
  }
}
