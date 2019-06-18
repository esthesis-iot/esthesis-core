package esthesis.platform.server.resource.dt;

import esthesis.extension.datasink.dto.DataSinkMeasurement;
import esthesis.platform.server.service.DTService;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/dt")
public class DTResource {

  private final DTService dtService;

  public DTResource(DTService dtService) {
    this.dtService = dtService;
  }

  private ResponseEntity<DataSinkMeasurement> returnEmptyResult() {
    return returnResult(null);
  }

  private <T> ResponseEntity<T> returnResult(T result) {
    if (result != null) {
      return ResponseEntity.ok(result);
    } else {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
  }

  @GetMapping(path = "/{hardwareId}/{mqttEventType}/{measurement}")
  public ResponseEntity<DataSinkMeasurement> get(
    @NotNull @PathVariable String hardwareId,
    @NotNull @PathVariable String mqttEventType,
    @NotNull @PathVariable String measurement,
    @RequestParam(required = false) Long from,
    @RequestParam(required = false) Long to,
    @RequestParam(required = false) String fields,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer pageSize) {

    return returnResult(
      dtService.get(hardwareId, mqttEventType, measurement, from, to, page, pageSize, fields));
  }

  @GetMapping(path = "/{hardwareId}/{mqttEventType}/{measurement}/{action}")
  public ResponseEntity<DataSinkMeasurement> count(
    @NotNull @PathVariable String hardwareId,
    @NotNull @PathVariable String mqttEventType,
    @NotNull @PathVariable String measurement,
    @NotNull @PathVariable String action,
    @RequestParam(required = false) Long from,
    @RequestParam(required = false) Long to,
    @RequestParam(required = false) String fields) {

    switch (action) {
      case "last":
        return returnResult(dtService.getLast(hardwareId, mqttEventType, measurement, fields));
      case "first":
        return returnResult(dtService.getFirst(hardwareId, mqttEventType, measurement, fields));
      case "count":
        return returnResult(dtService.count(hardwareId, mqttEventType, measurement, from, to, fields));
      case "max":
        if (StringUtils.isBlank(fields)) {
          return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        return returnResult(dtService.max(hardwareId, mqttEventType, measurement, from, to, fields));
      case "min":
        if (StringUtils.isBlank(fields)) {
          return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        return returnResult(dtService.min(hardwareId, mqttEventType, measurement, from, to, fields));
      case "average":
        if (StringUtils.isBlank(fields)) {
          return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        return returnResult(dtService.average(hardwareId, mqttEventType, measurement, from, to, fields));
      case "sum":
        if (StringUtils.isBlank(fields)) {
          return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        return returnResult(dtService.sum(hardwareId, mqttEventType, measurement, from, to, fields));
      default:
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

}
