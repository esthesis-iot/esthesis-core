package esthesis.platform.backend.server.service;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QMismatchException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.backend.common.device.commands.CommandRequestDTO;
import esthesis.platform.backend.server.config.AppConstants.DigitalTwins.Type;
import esthesis.platform.backend.server.dto.NiFiDTO;
import esthesis.platform.backend.server.model.Device;
import esthesis.platform.backend.server.repository.DeviceRepository;
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

import static esthesis.platform.backend.server.config.AppConstants.DigitalTwins.DTOperations.*;

@Log
@Service
@Validated
@Transactional
public class DTService {

  private final DeviceRepository deviceRepository;
  private final RestTemplate restTemplate;
  private final NiFiService niFiService;
  private final ObjectMapper objectMapper;

  public DTService(DeviceRepository deviceRepository, RestTemplate restTemplate,
                   NiFiService niFiService, ObjectMapper objectMapper) {
    this.deviceRepository = deviceRepository;
    this.restTemplate = restTemplate;
    this.niFiService = niFiService;
    this.objectMapper = objectMapper;
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
      .noneMatch(o -> o.equalsIgnoreCase(operation))) {
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

  /**
   * Executes an operation against NiFi and extracts the value of the given field.
   *
   * @param type        The type of measurement (e.g. telemetry, metadata)/
   * @param hardwareId  The hardware Id of the device to query.
   * @param operation   The type of operation to perform (QUERY, COUNT, MIN, etc.).
   * @param measurement The name of the measurement holding the values.
   * @param field       The field to extract from the given measurement.
   * @param from        Lower bound date/time restriction (in msec).
   * @param to          Upper bound date/time restriction (in msec).
   * @param page        The results page to return.
   * @param pageSize    The number of results on each page.
   * @return Returns an Object representation of the value of the requested field. If multiple values are obtained
   * from NiFi, the first one is picked.
   */
  public String extractMetadataOrTelemetrySingleValue(String type, String hardwareId, String operation,
                                                      String measurement, String field, Long from, Long to, Integer page,
                                                      Integer pageSize) throws JsonProcessingException {
    String retVal = null;
    if (field.contains(",")) {
      throw new QMismatchException("Only a single field is supported.");
    }
    String reply = executeMetadataOrTelemetry(type, hardwareId, operation, measurement, field, from, to, page, pageSize);
    if (StringUtils.isNotBlank(reply) && !StringUtils.replace(reply, " ", "").equals("{}")) {
      JsonNode measurementNode = objectMapper.readTree(reply).get(measurement);
      if (measurementNode != null && measurementNode.isArray() && measurementNode.size() > 0) {
        retVal = measurementNode.get(0).get(field).asText();
      }
    }

    return retVal;
  }

  /**
   * Executes an operation against NiFi and returns the JSON reply.
   *
   * @param type        The type of measurement (e.g. telemetry, metadata)/
   * @param hardwareId  The hardware Id of the device to query.
   * @param operation   The type of operation to perform (QUERY, COUNT, MIN, etc.).
   * @param measurement The name of the measurement holding the values.
   * @param fields      A comma-separated list of fields to extract from the given measurement.
   * @param from        Lower bound date/time restriction (in msec).
   * @param to          Upper bound date/time restriction (in msec).
   * @param page        The results page to return.
   * @param pageSize    The number of results on each page.
   * @return Returns the JSON reply from nifi, e.g.
   * {"health":[{"cpuTemperature":30.2,"timestamp":1612787754010,"type":"telemetry"}]}
   */
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

  public String executeCommand(String hardwareId, String operation, String description,
                               String args) {
    // TODO security checks
    // ...

    NiFiDTO niFiDTO = getActiveNifi();
    final UriComponentsBuilder request = UriComponentsBuilder.fromHttpUrl(niFiDTO.getDtUrl());
    request.pathSegment("dt", hardwareId, Type.COMMAND, operation);
    CommandRequestDTO body = new CommandRequestDTO();
    body.setDescription(description);
    body.setCreatedBy(
      SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    body.setArgs(args);

    log.finest(MessageFormat.format("Executing NiFi call: {0}", request.build().toUriString()));
    return restTemplate.postForEntity(request.build().toUri(), body, String.class).getBody();
  }

}
