package esthesis.services.registry.impl.resource;

import esthesis.service.registry.dto.RegistryEntry;
import esthesis.service.registry.resource.RegistryResourceV1;
import esthesis.services.registry.impl.service.RegistryService;
import java.util.List;
import javax.inject.Inject;

public class RegistryResourceV1Impl implements RegistryResourceV1 {

  @Inject
  RegistryService registryService;

//  @Override
//  public RegistryEntry findById(ObjectId id) {
//    return registryService.findById(id);
//  }

  @Override
  public RegistryEntry findByName(String name) {
    return registryService.findByName(name);
  }

  @Override
  public List<RegistryEntry> findByNames(String names) {
    return registryService.findByNames(names);
  }

  @Override
  public void save(RegistryEntry... registryEntry) {
    // Saving a registry entry is a special case as the caller might want to
    // overwrite the value of a registry entry by name (i.e. without knowing
    // the registry entry id).
    for (RegistryEntry entry : registryEntry) {
      if (entry.getId() != null) {
        registryService.save(entry);
      } else {
        RegistryEntry existingEntry = registryService.findByName(
            entry.getName());
        if (existingEntry != null) {
          entry.setId(existingEntry.getId());
        } else {
          registryService.save(entry);
        }
      }
    }
  }

//  @Override
//  public void saveMany(List<RegistryEntry> registryEntries) {
//    registryEntries.forEach(
//        registryEntry -> registryService.save(registryEntry));
//  }
}
