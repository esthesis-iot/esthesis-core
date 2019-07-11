package esthesis.platform.server.resource.dt;

import esthesis.extension.datasink.dto.DataSinkQueryResult;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
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

import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/dt")
public class DTResource {

  private final DTService dtService;
  private final DeviceRepository deviceRepository;

  public DTResource(DTService dtService,
    DeviceRepository deviceRepository) {
    this.dtService = dtService;
    this.deviceRepository = deviceRepository;
  }

  private ResponseEntity<DataSinkQueryResult> returnEmptyResult() {
    return returnResult(null);
  }

  private <T> ResponseEntity<T> returnResult(T result) {
    if (result != null) {
      return ResponseEntity.ok(result);
    } else {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
  }

  /**
   * Returns the value of one or more fields for a measurement.
   *
   * @param hardwareId The id of the device for which the results should be obtained.
   * @param mqttEventType The type of the MQTT event under which results were submitted (currently,
   * telemetry or metadata).
   * @param from The earliest EPOCH to return results from.
   * @param to The latest EPOCH to return results from.
   * @param fields A comma-separated list of fields to retrieve, or * for all fields for data sinks
   * that support it.
   * @param measurement The measurement for which to retrieve fields.
   * @param page The page of the results in case a pageable reply is wanted. First page is 0.
   * @param pageSize The size of each page of results.
   */
  @GetMapping(path = {"/{hardwareId}/{mqttEventType}", "/{hardwareId}/{mqttEventType}/{position}"})
  public ResponseEntity<DataSinkQueryResult> get(
    @NotNull @PathVariable String hardwareId,
    @NotNull @PathVariable String mqttEventType,
    @RequestParam(required = false) Long from,
    @RequestParam(required = false) Long to,
    @RequestParam(required = false) @NotNull String fields,
    @RequestParam(required = false) @NotNull String measurement,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) String position,
    @RequestParam(required = false) Integer pageSize) {

    if (StringUtils.isNotBlank(position)) {
      switch (position) {
        case "last":
          return returnResult(
            dtService.getLast(hardwareId, mqttEventType, measurement, fields.split(",")));
        case "first":
          return returnResult(
            dtService.getFirst(hardwareId, mqttEventType, measurement, fields.split(",")));
        default:
          return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
      }
    } else {
      return returnResult(
        dtService
          .get(hardwareId, mqttEventType, measurement, from, to, page, pageSize,
            StringUtils.defaultIfBlank(fields, "").split(",")));
    }
  }

  /**
   * Returns the count of values for a field (or all fields).
   *
   * @param hardwareId The id of the device for which the results should be obtained.
   * @param mqttEventType The type of the MQTT event under which results were submitted (currently,
   * telemetry or metadata).
   * @param from The earliest EPOCH to return results from.
   * @param to The latest EPOCH to return results from.
   * @param field The field to perform the action on, or empty for all fields.
   * @param measurement The measurement for which to retrieve fields.
   */
  @GetMapping(path = "/{hardwareId}/{mqttEventType}/calculate/count")
  public ResponseEntity<DataSinkQueryResult> count(
    @NotNull @PathVariable String hardwareId,
    @NotNull @PathVariable String mqttEventType,
    @RequestParam String measurement,
    @RequestParam(required = false) Long from,
    @RequestParam(required = false) Long to,
    @RequestParam(required = false) String field) {
    return returnResult(
      dtService.count(hardwareId, mqttEventType, measurement, from, to, field));
  }

  /**
   * Returns computed values of one or more fields for a measurement.
   *
   * @param hardwareId The id of the device for which the results should be obtained.
   * @param mqttEventType The type of the MQTT event under which results were submitted (currently,
   * telemetry or metadata).
   * @param from The earliest EPOCH to return results from.
   * @param to The latest EPOCH to return results from.
   * @param field The field to perform the action on.
   * @param measurement The measurement for which to retrieve fields.
   */
  @GetMapping(path = "/{hardwareId}/{mqttEventType}/calculate/{action}")
  public ResponseEntity<DataSinkQueryResult> calculate(
    @NotNull @PathVariable String hardwareId,
    @NotNull @PathVariable String mqttEventType,
    @RequestParam String measurement,
    @NotNull @PathVariable String action,
    @RequestParam(required = false) Long from,
    @RequestParam(required = false) Long to,
    @RequestParam String field) {

    switch (action) {
      case "max":
        return returnResult(
          dtService.max(hardwareId, mqttEventType, measurement, from, to, field));
      case "min":
        return returnResult(
          dtService.min(hardwareId, mqttEventType, measurement, from, to, field));
      case "average":
        return returnResult(
          dtService.average(hardwareId, mqttEventType, measurement, from, to, field));
      case "sum":
        return returnResult(
          dtService.sum(hardwareId, mqttEventType, measurement, from, to, field));
      default:
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Finds all devices registered after another device's registration date.
   *
   * @param hardwareId The device ID after which newer registrations are returned.
   * @return Returns a list of device IDs.
   */
  @GetMapping(path = "/registered-after-device")
  public ResponseEntity<List<String>> getDevicesRegisteredAfterDevice(
    @RequestParam String hardwareId) {
    final Optional<Device> device = deviceRepository.findByHardwareId(hardwareId);
    if (device.isPresent()) {
      return ResponseEntity.ok(dtService.getDevicesRegisteredAfter(device.get().getCreatedOn()));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }


}
