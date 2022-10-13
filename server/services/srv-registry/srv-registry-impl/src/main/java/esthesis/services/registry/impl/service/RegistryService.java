package esthesis.services.registry.impl.service;

import esthesis.common.AppConstants;
import esthesis.service.common.BaseService;
import esthesis.service.registry.dto.RegistryEntry;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class RegistryService extends BaseService<RegistryEntry> {

  public RegistryEntry findByName(AppConstants.Registry name) {
    log.debug("Looking up key '{}'.", name);
    RegistryEntry registryEntry = findByColumn("name", name.toString());
    log.debug("Found value '{}'.", registryEntry);

    return registryEntry;
  }

  public RegistryEntry findByTextName(String name) {
    log.debug("Looking up key '{}'.", name);
    RegistryEntry registryEntry = findByColumn("name", name);
    log.debug("Found value '{}'.", registryEntry);

    return registryEntry;
  }

}
