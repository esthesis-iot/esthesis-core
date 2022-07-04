package esthesis.services.registry.impl.service;

import esthesis.common.service.BaseService;
import esthesis.service.registry.dto.RegistryEntry;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class RegistryService extends BaseService<RegistryEntry> {

  public RegistryEntry findByName(String name) {
    log.debug("Looking up value '{}'.", name);
    RegistryEntry registryEntry = findByColumn("name", name);
    log.debug("Found value '{}'.", registryEntry);
    return registryEntry;
  }
}
