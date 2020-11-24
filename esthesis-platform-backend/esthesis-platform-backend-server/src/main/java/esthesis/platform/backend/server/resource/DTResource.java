package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import esthesis.platform.backend.common.device.commands.CommandRequestDTO;
import esthesis.platform.backend.server.config.AppConstants.DigitalTwins.Type;
import esthesis.platform.backend.server.dto.DTDeviceDTO;
import esthesis.platform.backend.server.model.Device;
import esthesis.platform.backend.server.repository.DeviceRepository;
import esthesis.platform.backend.server.service.DTService;
import esthesis.platform.backend.server.service.DeviceService;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/dt")
public class DTResource {

  private final DTService dtService;
  private final DeviceRepository deviceRepository;
  private final DeviceService deviceService;

  public DTResource(DTService dtService, DeviceRepository deviceRepository,
    DeviceService deviceService) {
    this.dtService = dtService;
    this.deviceRepository = deviceRepository;
    this.deviceService = deviceService;
  }

  @PostMapping(path = {"/{hardwareId}/" + Type.COMMAND + "/request/{operation}"})
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute command.")
  public String executeCommand(@NotNull @PathVariable final String hardwareId,
    @NotNull @PathVariable final String operation,
    @RequestBody CommandRequestDTO controlCommandRequest) {
    return dtService.executeCommand(hardwareId, operation, controlCommandRequest.getDescription(),
      controlCommandRequest.getArgs());
  }

  @GetMapping(path = {"/{hardwareId}/" + Type.TELEMETRY + "/{operation}",
    "/{hardwareId}/{dataType}/{operation}/{position}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute telemetry request.")
  public String executeTelemetry(@NotNull @PathVariable final String hardwareId,
    @NotNull @PathVariable final String operation,
    @RequestParam(required = false) final Long from,
    @RequestParam(required = false) final Long to,
    @RequestParam(required = false) final String fields,
    @RequestParam(required = false) final String measurement,
    @RequestParam(required = false) final Integer page,
    @RequestParam(required = false) final Integer pageSize) {

    return dtService
      .executeMetadataOrTelemetry(Type.TELEMETRY, hardwareId, operation, measurement, fields, from,
        to, page, pageSize);
  }

  @GetMapping(path = {"/{hardwareId}/" + Type.METADATA + "/{operation}",
    "/{hardwareId}/{dataType}/{operation}/{position}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute metadata request.")
  public String executeMetadata(@NotNull @PathVariable final String hardwareId,
    @NotNull @PathVariable final String operation,
    @RequestParam(required = false) final Long from,
    @RequestParam(required = false) final Long to,
    @RequestParam(required = false) final String fields,
    @RequestParam(required = false) final String measurement,
    @RequestParam(required = false) final Integer page,
    @RequestParam(required = false) final Integer pageSize) {

    return dtService
      .executeMetadataOrTelemetry(Type.METADATA, hardwareId, operation, measurement, fields, from,
        to, page, pageSize);
  }

  /**
   * Finds all devices registered after another device's registration date.
   *
   * @param hardwareId The device ID after which newer registrations are returned.
   * @return Returns a list of hardware IDs.
   */
  @GetMapping(path = "/registered-after-device")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not get registered devices.")
  public ResponseEntity<List<String>> getDevicesRegisteredAfterDevice(
    @RequestParam String hardwareId) {
    final Optional<Device> device = deviceRepository.findByHardwareId(hardwareId);

    return device
      .map(value -> ResponseEntity.ok(dtService.getDevicesRegisteredAfter(value.getCreatedOn())))
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  /**
   * Finds all devices registered in esthesis and returns their status.
   */
  @GetMapping(path = "/devices")
  @ReplyFilter("-createdBy,-modifiedBy")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not obtain devices list.")
  public List<DTDeviceDTO> findAll() {
    return deviceService.findAllDT();
  }

  /**
   * Returns the devices registered after a specific date.
   *
   * @param epoch The date after which devices will be matched as an EPOCH value in milliseconds.
   * @return Returns a list of hardware IDs.
   */
  @GetMapping(path = "/registered-after-date")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not get registered devices.")
  public ResponseEntity<List<String>> getDevicesRegisteredAfterDate(
    @RequestParam long epoch) {
    return ResponseEntity.ok(dtService.getDevicesRegisteredAfter(Instant.ofEpochMilli(epoch)));
  }

  /**
   * Returns all devices registered.
   *
   * @return Returns a list of hardware IDs.
   */
  @GetMapping(path = "/all-registered-devices")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not get registered devices.")
  public ResponseEntity<List<String>> getAllRegisteredDevices() {
    return ResponseEntity.ok(dtService.getDevicesRegisteredAfter(Instant.EPOCH));
  }
}
