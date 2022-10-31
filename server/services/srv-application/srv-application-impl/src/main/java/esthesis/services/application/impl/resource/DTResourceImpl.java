package esthesis.services.application.impl.resource;

import esthesis.service.application.dto.DTValueReply;
import esthesis.service.application.resource.DTResource;
import esthesis.services.application.impl.service.DTService;
import javax.inject.Inject;

public class DTResourceImpl implements DTResource {

  @Inject
  DTService dtService;

  @Override
  public DTValueReply find(String hardwareId, String category,
      String measurement) {
    return dtService.find(hardwareId, category, measurement);
  }
}
