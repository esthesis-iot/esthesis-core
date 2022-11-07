package esthesis.services.device.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.device.dto.DeviceProfileField;
import esthesis.services.device.impl.repository.DeviceProfileFieldRepository;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class DeviceProfileService extends BaseService<DeviceProfileField> {

  @Inject
  DeviceProfileFieldRepository deviceProfileFieldRepository;

  public List<DeviceProfileField> saveProfile(String deviceId,
      Map<String, String> profile) {
    // Remove fields no longer present.
    deviceProfileFieldRepository.deleteFieldsNotIn(deviceId,
        profile.keySet().stream().toList());

    // Save the field.
    profile.forEach((key, value) -> {
      DeviceProfileField deviceProfileField = deviceProfileFieldRepository.findByDeviceIdAndName(
          deviceId, key).orElseThrow();
      deviceProfileField.setFieldValue(value);
      save(deviceProfileField);
    });

    return getProfile(deviceId);
  }

  public List<DeviceProfileField> getProfile(String deviceId) {
    return deviceProfileFieldRepository.findByDeviceId(deviceId);
  }

  public DeviceProfileField createProfileField(
      DeviceProfileField deviceProfileField) {
    return save(deviceProfileField);
  }

  public void deleteProfileField(String deviceId, String fieldName) {
    deviceProfileFieldRepository.deleteByDeviceIdAndName(deviceId, fieldName);
  }
}
