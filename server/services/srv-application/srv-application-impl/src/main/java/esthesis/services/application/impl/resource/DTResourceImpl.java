package esthesis.services.application.impl.resource;

import esthesis.service.application.dto.DTValueReply;
import esthesis.service.application.resource.DTResource;
import esthesis.services.application.impl.service.DTService;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class DTResourceImpl implements DTResource {

  @Inject
  DTService dtService;

  @Override
  public DTValueReply findJSON(String hardwareId, String category,
      String measurement) {
    return dtService.find(hardwareId, category, measurement);
  }

  @Override
  public String findPlain(String hardwareId, String category,
      String measurement) {
    return dtService.find(hardwareId, category, measurement).getValue()
        .toString();
  }

  @Override
  public List<DTValueReply> findAllJSON(String hardwareId, String category) {
    return dtService.findAll(hardwareId, category);
  }

  @Override
  public String findAllPlain(String hardwareId, String category) {
    return dtService.findAll(hardwareId, category).stream().map(val ->
        val.getMeasurement() + "=" +
            val.getValue()).collect(Collectors.joining("\n"));
  }

  @Override
  public String findMeasurements(String hardwareId, String category) {
    return dtService.findAll(hardwareId, category).stream().map(
        DTValueReply::getMeasurement).collect(Collectors.joining("\n"));
  }

}
