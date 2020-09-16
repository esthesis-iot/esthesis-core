package esthesis.platform.server.service;

import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class DTService {

  private final DeviceRepository deviceRepository;
  private final AppProperties appProperties;
  private final RestTemplate restTemplate;

  public DTService(DeviceRepository deviceRepository,
    AppProperties appProperties, RestTemplate restTemplate) {
    this.deviceRepository = deviceRepository;
    this.appProperties = appProperties;
    this.restTemplate = restTemplate;
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

}
