package esthesis.services.device.impl.service;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.common.BaseService;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.entity.DeviceProfileNoteEntity;
import esthesis.service.settings.entity.DevicePageFieldEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.services.device.impl.repository.DeviceProfileFieldRepository;
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
public class DeviceProfileService extends BaseService<DeviceProfileNoteEntity> {

  @Inject
  DeviceProfileFieldRepository deviceProfileFieldRepository;

  @Inject
  @RestClient
  SettingsResource settingsResource;

  @Inject
  DeviceService deviceService;

  @Inject
  RedisUtils redisUtils;

  @Audited(op = Operation.WRITE, cat = Category.DEVICE, msg = "Save device profile")
  public List<DeviceProfileNoteEntity> saveProfile(String deviceId, Map<String, String> profile) {
    // Remove fields no longer present.
    deviceProfileFieldRepository.deleteFieldsNotIn(deviceId, profile.keySet().stream().toList());

    // Save the field.
    profile.forEach((key, value) -> {
      DeviceProfileNoteEntity deviceProfileNoteEntity = deviceProfileFieldRepository.findByDeviceIdAndName(
          deviceId, key).orElseThrow();
      deviceProfileNoteEntity.setFieldValue(value);
      save(deviceProfileNoteEntity);
    });

    return getProfile(deviceId);
  }

  public List<DeviceProfileNoteEntity> getProfile(String deviceId) {
    return deviceProfileFieldRepository.findByDeviceId(deviceId);
  }

  public DeviceProfileNoteEntity createProfileField(
      DeviceProfileNoteEntity deviceProfileNoteEntity) {
    return save(deviceProfileNoteEntity);
  }

  public void deleteProfileField(String deviceId, String fieldName) {
    deviceProfileFieldRepository.deleteByDeviceIdAndName(deviceId, fieldName);
  }

  public List<DeviceProfileFieldDataDTO> getProfileFields(String deviceId) {
    List<DeviceProfileFieldDataDTO> fields = new ArrayList<>();

    // Get configured device profile fields data.
    List<DevicePageFieldEntity> devicePageFieldEntities = settingsResource.getDevicePageFields();

    // Find the value of each field.
    DeviceEntity deviceEntity = deviceService.findById(deviceId);
    devicePageFieldEntities.stream().filter(DevicePageFieldEntity::isShown).forEach(field -> {
      DeviceProfileFieldDataDTO deviceProfileFieldDataDTO = new DeviceProfileFieldDataDTO();
      deviceProfileFieldDataDTO.setLabel(field.getLabel()).setIcon(field.getIcon())
          .setValueType(redisUtils.getFromHash(KeyType.ESTHESIS_DM, deviceEntity.getHardwareId(),
              field.getMeasurement()))
          .setLastUpdate(redisUtils.getLastUpdate(KeyType.ESTHESIS_DM, deviceEntity.getHardwareId(),
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
