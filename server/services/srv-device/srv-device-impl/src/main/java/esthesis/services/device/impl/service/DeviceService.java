package esthesis.services.device.impl.service;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.device.dto.GeolocationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.services.device.impl.repository.DeviceRepository;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
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
public class DeviceService extends BaseService<DeviceEntity> {

  @Inject
  JsonWebToken jwt;

  @Inject
  DeviceRepository deviceRepository;

  @Inject
  @RestClient
  SettingsResource settingsResource;

  @Inject
  RedisUtils redisUtils;

  /**
   * Finds a device by its hardware ID.
   *
   * @param hardwareId   The hardware ID to search by.
   * @param partialMatch Whether the search for the hardware ID should be partial or not.
   */
  public Optional<DeviceEntity> findByHardwareId(String hardwareId, boolean partialMatch) {
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
   * @param partialMatch Whether the search for the hardware ID will be partial or not.
   * @return Returns the list of devices matched.
   */
  public List<DeviceEntity> findByHardwareId(List<String> hardwareIds, boolean partialMatch) {
    if (partialMatch) {
      return deviceRepository.findByHardwareIdPartial(hardwareIds);
    } else {
      return deviceRepository.findByHardwareId(hardwareIds);
    }
  }

  /**
   * Counts the devices in a list of hardware Ids. Search takes place via an exact match algorithm.
   *
   * @param hardwareIds  The list of hardware Ids to check.
   * @param partialMatch Whether the search for the hardware ID will be partial or not.
   * @return The number of the devices in the list that matched.
   */
  public long countByHardwareId(List<String> hardwareIds, boolean partialMatch) {
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
    SettingEntity settingEntityLon = settingsResource.findByName(NamedSetting.DEVICE_GEO_LON);
    SettingEntity settingEntityLat = settingsResource.findByName(NamedSetting.DEVICE_GEO_LAT);

    if (settingEntityLon != null && settingEntityLat != null) {
      String hardwareId = findById(deviceId).getHardwareId();
      String redisLat = redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId,
          settingEntityLat.getValue());
      String redisLon = redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId,
          settingEntityLon.getValue());
      Instant lastUpdateLat = redisUtils.getLastUpdate(KeyType.ESTHESIS_DM, hardwareId,
          settingEntityLat.getValue());
      Instant lastUpdateLon = redisUtils.getLastUpdate(KeyType.ESTHESIS_DM, hardwareId,
          settingEntityLon.getValue());
      if (redisLat != null && redisLon != null && lastUpdateLat != null && lastUpdateLon != null) {
        return new GeolocationDTO(new BigDecimal(redisLat), new BigDecimal(redisLon),
            lastUpdateLat.isAfter(lastUpdateLon) ? lastUpdateLat : lastUpdateLon);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public String getPublicKey(String id) {
    return findById(id).getDeviceKey().getPublicKey();
  }

  public String getPrivateKey(String id) {
    return findById(id).getDeviceKey().getPrivateKey();
  }

  public String getCertificate(String id) {
    return findById(id).getDeviceKey().getCertificate();
  }

  /**
   * Remove a tag from all devices.
   *
   * @param tagId
   */
  public void removeTag(String tagId) {
    //TODO to be implemented
  }

}
