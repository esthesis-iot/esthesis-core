package esthesis.services.about.impl.resource;

import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.about.resource.AboutResource;
import esthesis.services.about.impl.service.AboutService;
import javax.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class AboutResourceImpl implements AboutResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  AboutService aboutService;

  @Override
  public AboutGeneralDTO getGeneralInfo() {
    return aboutService.getGeneralInfo();
  }
}
