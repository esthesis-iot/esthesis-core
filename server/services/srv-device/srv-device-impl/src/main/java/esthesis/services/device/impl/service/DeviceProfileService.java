package esthesis.services.device.impl.service;

import esthesis.common.AppConstants.MessagingKafka.Action;
import esthesis.common.AppConstants.MessagingKafka.Component;
import esthesis.common.AppConstants.MessagingKafka.Subject;
import esthesis.service.common.BaseService;
import esthesis.service.common.notifications.KafkaNotification;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.settings.entity.DevicePageFieldEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.services.device.impl.repository.DeviceAttributeRepository;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import io.quarkus.qute.Qute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DeviceProfileService extends BaseService<DeviceAttributeEntity> {

  @Inject
  DeviceAttributeRepository deviceAttributeRepository;

  @Inject
  @RestClient
  SettingsResource settingsResource;

  @Inject
  DeviceService deviceService;

  @Inject
  RedisUtils redisUtils;

  @KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE_ATTRIBUTE,
      action = Action.UPDATE, idParamOrder = 0)
  public List<DeviceAttributeEntity> saveProfile(String deviceId, Map<String, String> profile) {
    // Remove fields no longer present.
    deviceAttributeRepository.deleteAttributesNotIn(deviceId, profile.keySet().stream().toList());

    // Save the field.
    profile.forEach((key, value) -> {
      DeviceAttributeEntity deviceAttributeEntity = deviceAttributeRepository.findByDeviceIdAndName(
          deviceId, key).orElseThrow();
      deviceAttributeEntity.setFieldValue(value);
      save(deviceAttributeEntity);
    });

    return getAttributes(deviceId);
  }

  public List<DeviceAttributeEntity> getAttributes(String deviceId) {
    return deviceAttributeRepository.findByDeviceId(deviceId);
  }

  @KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE_ATTRIBUTE,
      action = Action.CREATE, idParamOrder = 0)
  public DeviceAttributeEntity createAttribute(String deviceId,
      DeviceAttributeEntity deviceAttributeEntity) {
    deviceAttributeEntity.setDeviceId(deviceId);
    return save(deviceAttributeEntity);
  }

  @KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE_ATTRIBUTE,
      action = Action.DELETE, idParamOrder = 0)
  public void deleteAttribute(String deviceId, String fieldName) {
    deviceAttributeRepository.deleteByDeviceIdAndName(deviceId, fieldName);
  }

  public List<DeviceProfileFieldDataDTO> getProfileFields(String deviceId) {
    List<DeviceProfileFieldDataDTO> fields = new ArrayList<>();

    // Get configured device profile fields data.
    List<DevicePageFieldEntity> devicePageFieldEntities = settingsResource.getDevicePageFields();

    // Find the value of each field.
    DeviceEntity deviceEntity = deviceService.findById(deviceId);
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
    DeviceEntity deviceEntity = deviceService.findById(deviceId);

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
