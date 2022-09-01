package esthesis.services.registry.impl.resource;

import esthesis.common.AppConstants;
import esthesis.service.registry.dto.RegistryEntry;
import esthesis.service.registry.resource.RegistrySystemResource;
import esthesis.services.registry.impl.service.RegistryService;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistrySystemResourceImpl implements RegistrySystemResource {

  @Inject
  RegistryService registryService;

  @Override
  public RegistryEntry findByName(AppConstants.Registry name) {
    return registryService.findByName(name);
  }
}
