package esthesis.platform.server.resource.dt;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import esthesis.common.datasink.dto.DataSinkQueryResult;
import esthesis.platform.server.dto.DTDeviceDTO;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
import esthesis.platform.server.service.DTService;

import javax.validation.constraints.NotNull;

import esthesis.platform.server.service.DeviceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    public DTResource(DTService dtService,
                      DeviceRepository deviceRepository, DeviceService deviceService) {
        this.dtService = dtService;
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
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
     * @param hardwareId    The id of the device for which the results should be obtained.
     * @param mqttEventType The type of the MQTT event under which results were submitted (currently,
     *                      telemetry or metadata).
     * @param from          The earliest EPOCH to return results from.
     * @param to            The latest EPOCH to return results from.
     * @param fields        A comma-separated list of fields to retrieve, or * for all fields for data sinks
     *                      that support it.
     * @param measurement   The measurement for which to retrieve fields.
     * @param page          The page of the results in case a pageable reply is wanted. First page is 0.
     * @param pageSize      The size of each page of results.
     * @param position      'first' to get the first measurement, 'last' to get the last measurement, or empty to get all measurements.
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
     * @param hardwareId    The id of the device for which the results should be obtained.
     * @param mqttEventType The type of the MQTT event under which results were submitted (currently,
     *                      telemetry or metadata).
     * @param from          The earliest EPOCH to return results from.
     * @param to            The latest EPOCH to return results from.
     * @param field         The field to perform the action on, or empty for all fields.
     * @param measurement   The measurement for which to retrieve fields.
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
     * @param hardwareId    The id of the device for which the results should be obtained.
     * @param mqttEventType The type of the MQTT event under which results were submitted (currently,
     *                      telemetry or metadata).
     * @param from          The earliest EPOCH to return results from.
     * @param to            The latest EPOCH to return results from.
     * @param field         The field to perform the action on.
     * @param measurement   The measurement for which to retrieve fields.
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
     * @return Returns a list of hardware IDs.
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


    /**
     * Finds all devices registered in esthesis and returns their status.
     * This API endpoint is a copy of the https://esthesis.bengr.prod.ath.esthesis.cloud/api/devices?size=100 call
     * but without the size
     * @return
     */
    @GetMapping(path = "/devices")
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
    public ResponseEntity<List<String>> getAllRegisteredDevices() {
        return ResponseEntity.ok(dtService.getDevicesRegisteredAfter(Instant.EPOCH));
    }
}
