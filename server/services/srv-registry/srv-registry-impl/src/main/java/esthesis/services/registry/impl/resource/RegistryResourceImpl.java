package esthesis.services.registry.impl.resource;

import esthesis.common.AppConstants;
import esthesis.service.registry.dto.RegistryEntry;
import esthesis.service.registry.resource.RegistryResource;
import esthesis.services.registry.impl.service.RegistryService;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
public class RegistryResourceImpl implements RegistryResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  RegistryService registryService;

  @Override
  public RegistryEntry findByName(AppConstants.Registry name) {
    return registryService.findByName(name);
  }

  @Override
  public List<RegistryEntry> findByNames(String names) {
    return Arrays.stream(names.split(",")).map(
            name -> registryService.findByName(AppConstants.Registry.valueOf(name)))
        .toList();
  }

  @Override
  public void save(RegistryEntry... registryEntry) {
    // Saving a registry entry is a special case as the caller might want to
    // overwrite the value of a registry entry by name (i.e. without knowing
    // the registry entry id).
    for (RegistryEntry entry : registryEntry) {
      if (entry.getId() != null) {
        log.debug("Updating an existing registry entry by id with '{}'.",
            entry);
        registryService.save(entry);
      } else {
        RegistryEntry existingEntry = registryService.findByTextName(
            entry.getName());
        if (existingEntry != null) {
          log.debug("Updating an existing registry entry with '{}'.", entry);
          entry.setId(existingEntry.getId());
          registryService.save(entry);
        } else {
          log.debug("Creating a new registry entry '{}'.", entry);
          registryService.save(entry);
        }
      }
    }
  }
}
