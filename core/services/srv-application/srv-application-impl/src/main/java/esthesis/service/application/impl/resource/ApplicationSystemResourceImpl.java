package esthesis.service.application.impl.resource;

import esthesis.service.application.impl.service.ApplicationService;
import esthesis.service.application.resource.ApplicationSystemResource;
import javax.inject.Inject;

public class ApplicationSystemResourceImpl implements ApplicationSystemResource {

  @Inject
  ApplicationService applicationService;
  
  @Override
  public boolean isTokenValid(String token) {
    return applicationService.isTokenValid(token);
  }
}
