package esthesis.services.device.impl.service;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.dto.GeolocationDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.settings.entity.DevicePageFieldEntity;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.services.device.impl.repository.DeviceAttributeRepository;
import esthesis.services.device.impl.repository.DeviceRepository;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import esthesis.util.kafka.notifications.outgoing.KafkaNotification;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import io.quarkus.qute.Qute;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
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
  DeviceAttributeRepository deviceAttributeRepository;

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

  @KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE_ATTRIBUTE,
      action = Action.UPDATE, idParamOrder = 0, payload = "device id")
  public List<DeviceAttributeEntity> saveAttributes(String deviceId, Map<String, String> profile) {
    // Remove fields no longer present.
    deviceAttributeRepository.deleteAttributesNotIn(deviceId, profile.keySet().stream().toList());

    // Save the field.
    profile.forEach((key, value) -> {
      DeviceAttributeEntity deviceAttributeEntity = deviceAttributeRepository.findByDeviceIdAndName(
          deviceId, key).orElseThrow();
      deviceAttributeEntity.setAttributeValue(value);
      deviceAttributeRepository.update(deviceAttributeEntity);
    });

    return getAttributes(deviceId);
  }

  public List<DeviceAttributeEntity> getAttributes(String deviceId) {
    return deviceAttributeRepository.findByDeviceId(deviceId);
  }

  @KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE_ATTRIBUTE,
      action = Action.CREATE, idParamOrder = 0, payload = "device id")
  public DeviceAttributeEntity createAttribute(String deviceId,
      DeviceAttributeEntity deviceAttributeEntity) {
    deviceAttributeEntity.setDeviceId(deviceId);
    deviceAttributeEntity.setId(new ObjectId());
    deviceAttributeRepository.persist(deviceAttributeEntity);

    return deviceAttributeEntity;
  }

  @KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE_ATTRIBUTE,
      action = Action.DELETE, idParamOrder = 0, payload = "device id")
  public void deleteAttribute(String deviceId, String fieldName) {
    deviceAttributeRepository.deleteByDeviceIdAndName(deviceId, fieldName);
  }

  public List<DeviceProfileFieldDataDTO> getProfileFields(String deviceId) {
    List<DeviceProfileFieldDataDTO> fields = new ArrayList<>();

    // Get configured device profile fields data.
    List<DevicePageFieldEntity> devicePageFieldEntities = settingsResource.getDevicePageFields();

    // Find the value of each field.
    DeviceEntity deviceEntity = findById(deviceId);
    devicePageFieldEntities.stream().filter(DevicePageFieldEntity::isShown).forEach(field -> {
      DeviceProfileFieldDataDTO deviceProfileFieldDataDTO = new DeviceProfileFieldDataDTO();
      deviceProfileFieldDataDTO.setLabel(field.getLabel()).setIcon(field.getIcon()).setValueType(
          redisUtils.getFromHash(KeyType.ESTHESIS_DM, deviceEntity.getHardwareId(),
              field.getMeasurement())).setLastUpdate(
          redisUtils.getLastUpdate(KeyType.ESTHESIS_DM, deviceEntity.getHardwareId(),
              field.getMeasurement()));

      String value = redisUtils.getFromHash(KeyType.ESTHESIS_DM, deviceEntity.getHardwareId(),
          field.getMeasurement());
      if (StringUtils.isNotEmpty(field.getFormatter())) {
        deviceProfileFieldDataDTO.setValue(
            Qute.fmt(field.getFormatter()).data("val", value).render());
      } else {
        deviceProfileFieldDataDTO.setValue(value);
      }

      fields.add(deviceProfileFieldDataDTO);
    });

    return fields;
  }

  public List<DeviceProfileFieldDataDTO> getAllDeviceData(String deviceId) {
    List<DeviceProfileFieldDataDTO> fields = new ArrayList<>();
    DeviceEntity deviceEntity = findById(deviceId);

    redisUtils.getHashTriplets(KeyType.ESTHESIS_DM, deviceEntity.getHardwareId())
        .forEach(triple -> {
          DeviceProfileFieldDataDTO deviceProfileFieldDataDTO = new DeviceProfileFieldDataDTO();
          deviceProfileFieldDataDTO.setLabel(triple.getLeft()).setValue(triple.getMiddle())
              .setLastUpdate(triple.getRight());
          fields.add(deviceProfileFieldDataDTO);
        });

    return fields;
  }
}
