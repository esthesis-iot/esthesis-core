package esthesis.services.about.impl.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.about.resource.AboutResource;
import esthesis.service.audit.ccc.Audited;
import esthesis.services.about.impl.service.AboutService;
import javax.inject.Inject;

public class AboutResourceImpl implements AboutResource {

  @Inject
  AboutService aboutService;

  @Override
  @Audited(cat = Category.ABOUT, op = Operation.RETRIEVE, msg = "About/General page")
  public AboutGeneralDTO getGeneralInfo() {
    return aboutService.getGeneralInfo();
  }
}
