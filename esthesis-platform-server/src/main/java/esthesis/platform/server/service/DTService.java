package esthesis.platform.server.service;

import static esthesis.platform.server.config.AppConstants.DTOperations.OPERATION_COUNT;
import static esthesis.platform.server.config.AppConstants.DTOperations.OPERATION_MAX;
import static esthesis.platform.server.config.AppConstants.DTOperations.OPERATION_MEAN;
import static esthesis.platform.server.config.AppConstants.DTOperations.OPERATION_MIN;
import static esthesis.platform.server.config.AppConstants.DTOperations.OPERATION_QUERY;
import static esthesis.platform.server.config.AppConstants.DTOperations.OPERATION_SUM;
import static esthesis.platform.server.config.AppConstants.DTOperations.SUPPORTED_OPERATIONS;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.common.exception.QMismatchException;
import esthesis.platform.server.dto.NiFiDTO;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
import javax.validation.constraints.NotNull;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
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

  public void aVoid() {

  }

  /**
   * Returns the list of devices registered
   */
  public List<String> getDevicesRegisteredAfter(Instant date) {
    return deviceRepository.findAllByCreatedOnAfter(date).stream()
      .map(Device::getHardwareId)
      .collect(Collectors.toList());
  }

  /**
   *
   * @param hardwareId
   * @param dataType
   * @param operation
   * @param from
   * @param to
   * @param fields
   * @param measurement
   * @param page A number >=1 indicating the starting page of the results.
   * @param pageSize A number >=1 indicated the numer of results on each results page.
   * @return
   */
  public String nifiProxy(@NotNull final String hardwareId, @NotNull final String dataType,
    @NotNull final String operation, final Long from, final Long to,
    final String fields, final String measurement, Integer page, final Integer pageSize) {

    // TODO security checks
    // ...

    // Check if requested operation exists; if not, throw an exception.
    if (Arrays.stream(SUPPORTED_OPERATIONS).noneMatch(o -> o.equals(operation.toUpperCase()))) {
      throw new QDoesNotExistException("Operation {} is not supported", operation);
    }



    // Find the currently active NiFi instance.
    final NiFiDTO activeNiFi = niFiService.getActiveNiFi();
    if (activeNiFi == null) {
      throw new QDoesNotExistException("Could not find an active NiFi instance.");
    } else if (StringUtils.isBlank(activeNiFi.getDtUrl())) {
      throw new QMismatchException(
        "The currently active NiFi instance is not configured with a Digital Twins URL.");
    }

    // According to the operation requested, prepare the REST request to NiFi.
    final UriComponentsBuilder request = UriComponentsBuilder
      .fromHttpUrl(activeNiFi.getDtUrl())
      .pathSegment("dt", hardwareId, dataType, operation);

    // Add common query parameters.
    if (StringUtils.isEmpty(measurement)) {
      throw new QDoesNotExistException(
        "Operation \"{0}\" needs a \"measurement\" parameter.", operation);
    } else {
      request.queryParam("measurement", measurement);
    }

    // Add operation-specific parameters.
    switch (operation.toUpperCase()) {
      case OPERATION_QUERY:
        if (StringUtils.isNotEmpty(fields)) {
          request.queryParam("fields", fields);
        }
        if (from != null) {
          request.queryParam("from", from);
        }
        if (to != null) {
          request.queryParam("to", to);
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
        if (from != null) {
          request.queryParam("from", from);
        }
        if (to != null) {
          request.queryParam("to", to);
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

    log.finest(MessageFormat.format("Executing NiFi query: {0}", request.build().toUriString()));

    return restTemplate.getForEntity(request.build().toUri(), String.class).getBody();
  }
}
