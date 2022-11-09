package esthesis.services.device.impl.service;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.GeolocationDTO;
import esthesis.service.settings.dto.Setting;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.services.device.impl.repository.DeviceRepository;
import esthesis.util.redis.EsthesisRedis;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DeviceService extends BaseService<Device> {

  @Inject
  JsonWebToken jwt;

  @Inject
  DeviceRepository deviceRepository;

  @Inject
  @RestClient
  SettingsResource settingsResource;

  @Inject
  EsthesisRedis redis;

  /**
   * Finds a device by its hardware ID.
   *
   * @param hardwareId   The hardware ID to search by.
   * @param partialMatch Whether the search for the hardware ID should be
   *                     partial or not.
   */
  public Optional<Device> findByHardwareId(String hardwareId,
      boolean partialMatch) {
    if (partialMatch) {
      return deviceRepository.findByHardwareIdPartial(hardwareId);
    } else {
      return deviceRepository.findByHardwareId(hardwareId);
    }
  }

  /**
   * Finds the devices by a list of hardware Ids.
   *
   * @param hardwareIds  The list of hardware IDs to check.
   * @param partialMatch Whether the search for the hardware ID will be partial
   *                     or not.
   * @return Returns the list of devices matched.
   */
  public List<Device> findByHardwareId(List<String> hardwareIds,
      boolean partialMatch) {
    if (partialMatch) {
      return deviceRepository.findByHardwareIdPartial(hardwareIds);
    } else {
      return deviceRepository.findByHardwareId(hardwareIds);
    }
  }

  /**
   * Counts the devices in a list of hardware Ids. Search takes place via an
   * exact match algorithm.
   *
   * @param hardwareIds  The list of hardware Ids to check.
   * @param partialMatch Whether the search for the hardware ID will be partial
   *                     or not.
   * @return The number of the devices in the list that matched.
   */
  public long countByHardwareId(List<String> hardwareIds,
      boolean partialMatch) {
    if (partialMatch) {
      return deviceRepository.countByHardwareIdPartial(hardwareIds);
    } else {
      return deviceRepository.countByHardwareId(hardwareIds);
    }
  }

  /**
   * Returns the last known geolocation attributes of a device.
   *
   * @param deviceId The device ID to search by.
   */
  public GeolocationDTO getGeolocation(String deviceId) {
    Setting settingLon = settingsResource.findByName(
        NamedSetting.DEVICE_GEO_LON);
    Setting settingLat = settingsResource.findByName(
        NamedSetting.DEVICE_GEO_LAT);

    if (settingLon != null && settingLat != null) {
      String hardwareId = findById(deviceId).getHardwareId();
      String redisLat = redis.getValue(hardwareId, settingLat.getValue());
      String redisLon = redis.getValue(hardwareId, settingLon.getValue());
      Instant lastUpdateLat = redis.getLastUpdate(hardwareId,
          settingLat.getValue());
      Instant lastUpdateLon = redis.getLastUpdate(hardwareId,
          settingLon.getValue());
      if (redisLat != null && redisLon != null && lastUpdateLat != null
          && lastUpdateLon != null) {
        return new GeolocationDTO(new BigDecimal(redisLat),
            new BigDecimal(redisLon),
            lastUpdateLat.isAfter(lastUpdateLon) ? lastUpdateLat
                : lastUpdateLon);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
}
