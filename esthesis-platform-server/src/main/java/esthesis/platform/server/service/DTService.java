package esthesis.platform.server.service;


import static esthesis.platform.server.config.AppConstants.DigitalTwins.DTOperations.OPERATION_COUNT;
import static esthesis.platform.server.config.AppConstants.DigitalTwins.DTOperations.OPERATION_MAX;
import static esthesis.platform.server.config.AppConstants.DigitalTwins.DTOperations.OPERATION_MEAN;
import static esthesis.platform.server.config.AppConstants.DigitalTwins.DTOperations.OPERATION_MIN;
import static esthesis.platform.server.config.AppConstants.DigitalTwins.DTOperations.OPERATION_QUERY;
import static esthesis.platform.server.config.AppConstants.DigitalTwins.DTOperations.OPERATION_SUM;
import static esthesis.platform.server.config.AppConstants.DigitalTwins.DTOperations.SUPPORTED_OPERATIONS;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QMismatchException;
import esthesis.common.device.control.ControlCommandRequest;
import esthesis.platform.server.config.AppConstants.DigitalTwins.Type;
import esthesis.platform.server.dto.NiFiDTO;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Log
@Service
@Validated
@Transactional
public class DTService {

  private final DeviceRepository deviceRepository;
  private final RestTemplate restTemplate;
  private final NiFiService niFiService;

  public DTService(DeviceRepository deviceRepository, RestTemplate restTemplate,
    NiFiService niFiService) {
    this.deviceRepository = deviceRepository;
    this.restTemplate = restTemplate;
    this.niFiService = niFiService;
  }

  /**
   * Returns the list of devices registered after a specific date.
   */
  public List<String> getDevicesRegisteredAfter(Instant date) {
    return deviceRepository.findAllByCreatedOnAfter(date).stream()
      .map(Device::getHardwareId)
      .collect(Collectors.toList());
  }

  private void checkAllowedOperationForTelemetryAndMetadata(String operation) {
    // Check if requested operation exists; if not, throw an exception.
    if (Arrays.stream(SUPPORTED_OPERATIONS)
      .noneMatch(o -> o.equals(operation.toUpperCase()))) {
      throw new QDoesNotExistException("Operation {} is not supported", operation);
    }
  }

  private NiFiDTO getActiveNifi() {
    // Find the currently active NiFi instance.
    final NiFiDTO activeNiFi = niFiService.getActiveNiFi();
    if (activeNiFi == null) {
      throw new QDoesNotExistException("Could not find an active NiFi instance.");
    } else if (StringUtils.isBlank(activeNiFi.getDtUrl())) {
      throw new QMismatchException(
        "The currently active NiFi instance is not configured with a Digital Twins URL.");
    }

    return activeNiFi;
  }

  private void addMeasurement(UriComponentsBuilder request, String operation, String measurement) {
    if (StringUtils.isEmpty(measurement)) {
      throw new QDoesNotExistException(
        "Operation \"{0}\" needs a \"measurement\" parameter.", operation);
    } else {
      request.queryParam("measurement", measurement);
    }
  }

  public String executeMetadataOrTelemetry(String type, String hardwareId, String operation,
    String measurement, String fields, Long from, Long to, Integer page, Integer pageSize) {
    // TODO security checks
    // ...

    checkAllowedOperationForTelemetryAndMetadata(operation);
    NiFiDTO niFiDTO = getActiveNifi();
    final UriComponentsBuilder request = UriComponentsBuilder.fromHttpUrl(niFiDTO.getDtUrl());
    addMeasurement(request, operation, measurement);

    // Add operation-specific parameters.
    if (from != null) {
      request.queryParam("from", from);
    }
    if (to != null) {
      request.queryParam("to", to);
    }
    switch (operation.toUpperCase()) {
      case OPERATION_QUERY:
        if (StringUtils.isNotEmpty(fields)) {
          request.queryParam("fields", fields);
        }
        if (page != null) {
          request.queryParam("page", page > 0 ? page - 1 : 0);
        }
        if (pageSize != null) {
          request.queryParam("pageSize", pageSize > 1 ? pageSize : 1);
        }
        break;
      case OPERATION_COUNT:
      case OPERATION_MAX:
      case OPERATION_MEAN:
      case OPERATION_MIN:
      case OPERATION_SUM:
        if (StringUtils.isNotEmpty(fields)) {
          request.queryParam("fields", fields);
        } else {
          throw new QDoesNotExistException(
            "Operation \"{0}\" needs a \"fields\" parameter.", operation);
        }
        if (page != null && pageSize != null) {
          throw new QDoesNotExistException(
            "Operation \"{0}\" does not support a \"page\" or \"pageSize\"parameter.", operation);
        }
        break;
      default:
        throw new QDoesNotExistException(
          "Operation \"{0}\" is not supported.", operation);
    }
    request.pathSegment("dt", hardwareId, type, operation);

    log.finest(MessageFormat.format("Executing NiFi call: {0}", request.build().toUriString()));
    return restTemplate.getForEntity(request.build().toUri(), String.class).getBody();
  }

  public String executeCommand(String hardwareId, String operation, String description, String args) {
    // TODO security checks
    // ...

    NiFiDTO niFiDTO = getActiveNifi();
    final UriComponentsBuilder request = UriComponentsBuilder.fromHttpUrl(niFiDTO.getDtUrl());
    request.pathSegment("dt", hardwareId, Type.COMMAND, operation);
    ControlCommandRequest body = new ControlCommandRequest();
    body.setDescription(description);
    body.setCreatedBy(
      SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    body.setArgs(args);

    log.finest(MessageFormat.format("Executing NiFi call: {0}", request.build().toUriString()));
    return restTemplate.postForEntity(request.build().toUri(), body, String.class).getBody();
  }

}
