package esthesis.services.about.impl.resource;

import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.about.resource.AboutResource;
import esthesis.services.about.impl.service.AboutService;
import javax.inject.Inject;

public class AboutResourceImpl implements AboutResource {

  @Inject
  AboutService aboutService;

  @Override
  public AboutGeneralDTO getGeneralInfo() {
    return aboutService.getGeneralInfo();
  }
}
