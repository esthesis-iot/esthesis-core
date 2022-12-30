package esthesis.service.dt.impl.resource;

import esthesis.service.dt.dto.DTValueReplyDTO;
import esthesis.service.dt.impl.service.DTService;
import esthesis.service.dt.resource.DTResource;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;

public class DTResourceImpl implements DTResource {

  @Inject
  DTService dtService;

  @Override
  public Response findJSON(String hardwareId, String category,
      String measurement) {
    DTValueReplyDTO dtValueReplyDTO = dtService.find(hardwareId, category, measurement);
    if (dtValueReplyDTO != null) {
      return Response.ok(dtValueReplyDTO).build();
    } else {
      return Response.status(Status.NO_CONTENT).build();
    }
  }

  @Override
  public Response findPlain(String hardwareId, String category,
      String measurement) {
    DTValueReplyDTO dtValueReplyDTO = dtService.find(hardwareId, category, measurement);
    if (dtValueReplyDTO != null) {
      return Response.ok(dtValueReplyDTO.getValue().toString()).build();
    } else {
      return Response.status(Status.NO_CONTENT).build();
    }
  }

  @Override
  public Response findAllJSON(String hardwareId, String category) {
    List<DTValueReplyDTO> values = dtService.findAll(hardwareId, category);
    if (values != null && !values.isEmpty()) {
      return Response.ok(values).build();
    } else {
      return Response.status(Status.NO_CONTENT).build();
    }
  }

  @Override
  public Response findAllPlain(String hardwareId, String category) {
    String values = dtService.findAll(hardwareId, category).stream().map(val ->
        val.getMeasurement() + "=" +
            val.getValue()).collect(Collectors.joining("\n"));
    if (StringUtils.isNotBlank(values)) {
      return Response.ok(values).build();
    } else {
      return Response.status(Status.NO_CONTENT).build();
    }
  }

  @Override
  public Response findMeasurements(String hardwareId, String category) {
    String values = dtService.findAll(hardwareId, category).stream().map(
        DTValueReplyDTO::getMeasurement).collect(Collectors.joining("\n"));
    if (StringUtils.isNotBlank(values)) {
      return Response.ok(values).build();
    } else {
      return Response.status(Status.NO_CONTENT).build();
    }
  }

}
