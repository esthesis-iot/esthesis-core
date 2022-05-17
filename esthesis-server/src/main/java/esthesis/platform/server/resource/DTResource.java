package esthesis.platform.server.resource;

import esthesis.platform.server.config.AppConstants;
import esthesis.platform.server.service.CommandReplyService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;

import esthesis.platform.server.config.AppConstants.Audit.Device.CommandType;
import esthesis.platform.server.config.AppConstants.DigitalTwins.Type;
import esthesis.platform.server.dto.device.CommandReplyDTO;
import esthesis.platform.server.dto.device.CommandRequestDTO;
import esthesis.platform.server.dto.device.DeviceDTO;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
import esthesis.platform.server.service.DTService;
import esthesis.platform.server.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/dt")
@SecurityRequirement(name = "bearerAuth")
public class DTResource {

  private final DTService dtService;
  private final DeviceRepository deviceRepository;
  private final DeviceService deviceService;
  private final CommandReplyService commandReplyService;

  @Operation(description = "Executes a command against a device.", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
    @ApiResponse(responseCode = "500", description = "Could not execute command", content = @Content),
  })
  @PostMapping(path = {"/{hardwareId}/command/request/{operation}"})
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute command.")
  public String executeCommand(@NotNull @PathVariable @Parameter(description = "The hardware Id of the device to be contacted.") final String hardwareId,
    @NotNull @PathVariable @Parameter(description = "The operation to be executed on the device.") final CommandType operation,
    @RequestBody @Parameter(description = "Contains all the needed information for the command execution") CommandRequestDTO controlCommandRequest) {
    return dtService.executeCommand(hardwareId, operation, controlCommandRequest.getDescription(), controlCommandRequest.getArgs());
  }

  @Operation(description = "Retrieves the results of a previously executed command.", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
    @ApiResponse(responseCode = "500", description = "Could not execute command", content = @Content),
  })
  @GetMapping(path = {"/command/reply/{commandId}"})
  public CommandReplyDTO getCommandReply(
    @PathVariable @Parameter(description = "The request Id for the command to retrieve the results for.") final @NotNull long commandId) {
    return commandReplyService.findByCommandRequestId(commandId);
  }

  @Operation(description = "Executes an operation on metadata against NiFi and returns the JSON reply.", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
    @ApiResponse(responseCode = "500", description = "Could not execute metadata request.", content = @Content),
  })
  @GetMapping(path = {
    "/{hardwareId}/metadata/{operation}", "/{hardwareId}/{dataType}/{operation}/{position}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute metadata request.")
  public String executeMetadata(
    @Parameter(description = "The hardware Id of the device to query.") @NotNull @PathVariable final String hardwareId,
    @Parameter(description = "The type of operation to perform (QUERY, COUNT, MIN, etc.).") @NotNull @PathVariable final AppConstants.DigitalTwins.DTOperations operation,
    @Parameter(description = "Lower bound date/time restriction (in msec).") @RequestParam(required = false) final Long from,
    @Parameter(description = "Upper bound date/time restriction (in msec).") @RequestParam(required = false) final Long to,
    @Parameter(description = "A comma-separated list of fields to extract from the given measurement.") @RequestParam(required = false) final String fields,
    @Parameter(description = "The name of the measurement holding the values.") @RequestParam(required = false) final String measurement,
    @Parameter(description = "The results page to return.") @RequestParam(required = false) final Integer page,
    @Parameter(description = "The number of results on each page.") @RequestParam(required = false) final Integer pageSize) {

    return dtService.executeMetadataOrTelemetry(Type.metadata, hardwareId, operation, measurement, fields, from, to, page, pageSize);
  }

  @Operation(description = "Executes an operation on telemetry against NiFi and returns the JSON reply.", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
    @ApiResponse(responseCode = "500", description = "Could not execute telemetry request.", content = @Content),
  })
  @GetMapping(path = {
    "/{hardwareId}/telemetry/{operation}", "/{hardwareId}/{dataType}/{operation}/{position}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute telemetry request.")
  public String executeTelemetry(
    @Parameter(description = "The hardware Id of the device to query.") @NotNull @PathVariable final String hardwareId,
    @Parameter(description = "The type of operation to perform (QUERY, COUNT, MIN, etc.).") @NotNull @PathVariable final AppConstants.DigitalTwins.DTOperations operation,
    @Parameter(description = "Lower bound date/time restriction (in msec).") @RequestParam(required = false) final Long from,
    @Parameter(description = "Upper bound date/time restriction (in msec).") @RequestParam(required = false) final Long to,
    @Parameter(description = "A comma-separated list of fields to extract from the given measurement.") @RequestParam(required = false) final String fields,
    @Parameter(description = "The name of the measurement holding the values.") @RequestParam(required = false) final String measurement,
    @Parameter(description = "The results page to return.") @RequestParam(required = false) final Integer page,
    @Parameter(description = "The number of results on each page.") @RequestParam(required = false) final Integer pageSize) {

    return dtService.executeMetadataOrTelemetry(Type.telemetry, hardwareId, operation, measurement, fields, from, to, page, pageSize);
  }

  /**
   * Finds all devices registered after another device's registration date.
   *
   * @param hardwareId The device ID after which newer registrations are returned.
   * @return Returns a list of hardware IDs.
   */
  @Operation(description = "Finds all devices registered after another device's registration date.", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
    @ApiResponse(responseCode = "500", description = "Could not get registered devices.", content = @Content),
  })
  @GetMapping(path = "/registered-after-device")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not get registered devices.")
  public ResponseEntity<List<String>> getDevicesRegisteredAfterDevice(
    @Parameter(description = "The device ID after which newer registrations are returned.") @RequestParam String hardwareId) {
    final Optional<Device> device = deviceRepository.findByHardwareId(hardwareId);

    return device.map(value -> ResponseEntity.ok(dtService.getDevicesRegisteredAfter(value.getCreatedOn())))
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  /**
   * Finds all registered devices.
   */
  @Operation(description = "Finds all registered devices.", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
    @ApiResponse(responseCode = "500", description = "Could not obtain devices list.", content = @Content),
  })
  @GetMapping(path = "/devices")
  @ReplyFilter("-createdBy,-modifiedBy")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not obtain devices list.")
  public List<DeviceDTO> findAll() {
    return deviceService.findAll();
  }

  /**
   * Returns the devices registered after a specific date.
   *
   * @param epoch The date after which devices will be matched as an EPOCH value in milliseconds.
   * @return Returns a list of hardware IDs.
   */
  @Operation(description = "Returns the devices registered after a specific date." , responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
    @ApiResponse(responseCode = "500", description = "Could not get registered devices.", content = @Content),
  })
  @GetMapping(path = "/registered-after-date")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not get registered devices.")
  public ResponseEntity<List<String>> getDevicesRegisteredAfterDate(@Parameter(description = "The date after which devices will be matched as an EPOCH value in milliseconds.") @RequestParam long epoch) {
    return ResponseEntity.ok(dtService.getDevicesRegisteredAfter(Instant.ofEpochMilli(epoch)));
  }

  /**
   * Returns all devices registered.
   *
   * @return Returns a list of hardware IDs.
   */
  @Operation(description = "Returns all registered devices.", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
    @ApiResponse(responseCode = "500", description = "Could not get registered devices.", content = @Content),
  })
  @GetMapping(path = "/all-registered-devices")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not get registered devices.")
  public ResponseEntity<List<String>> getAllRegisteredDevices() {
    return ResponseEntity.ok(dtService.getDevicesRegisteredAfter(Instant.EPOCH));
  }
}
