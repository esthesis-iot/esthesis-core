package esthesis.services.registry.impl.resource;

import esthesis.service.registry.dto.RegistryEntry;
import esthesis.service.registry.resource.RegistryResourceV1;
import esthesis.services.registry.impl.service.RegistryService;
import javax.inject.Inject;
import org.bson.types.ObjectId;

public class RegistryResourceV1Impl implements RegistryResourceV1 {

  @Inject
  RegistryService registryService;

  @Override
  public RegistryEntry findById(ObjectId id) {
    return registryService.findById(id);
  }

  @Override
  public RegistryEntry findByName(String name) {
    return registryService.findByName(name);
  }
}
