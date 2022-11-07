package esthesis.services.device.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.device.dto.DeviceProfileNote;
import esthesis.services.device.impl.repository.DeviceProfileFieldRepository;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class DeviceProfileService extends BaseService<DeviceProfileNote> {

  @Inject
  DeviceProfileFieldRepository deviceProfileFieldRepository;

  public List<DeviceProfileNote> saveProfile(String deviceId,
      Map<String, String> profile) {
    // Remove fields no longer present.
    deviceProfileFieldRepository.deleteFieldsNotIn(deviceId,
        profile.keySet().stream().toList());

    // Save the field.
    profile.forEach((key, value) -> {
      DeviceProfileNote deviceProfileNote = deviceProfileFieldRepository.findByDeviceIdAndName(
          deviceId, key).orElseThrow();
      deviceProfileNote.setFieldValue(value);
      save(deviceProfileNote);
    });

    return getProfile(deviceId);
  }

  public List<DeviceProfileNote> getProfile(String deviceId) {
    return deviceProfileFieldRepository.findByDeviceId(deviceId);
  }

  public DeviceProfileNote createProfileField(
      DeviceProfileNote deviceProfileNote) {
    return save(deviceProfileNote);
  }

  public void deleteProfileField(String deviceId, String fieldName) {
    deviceProfileFieldRepository.deleteByDeviceIdAndName(deviceId, fieldName);
  }
}
