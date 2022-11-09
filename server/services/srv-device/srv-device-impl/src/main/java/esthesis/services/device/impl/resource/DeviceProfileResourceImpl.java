package esthesis.services.device.impl.resource;

import esthesis.service.device.dto.DeviceProfileFieldData;
import esthesis.service.device.dto.DeviceProfileNote;
import esthesis.service.device.resource.DeviceProfileResource;
import esthesis.services.device.impl.service.DeviceProfileService;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class DeviceProfileResourceImpl implements DeviceProfileResource {

  @Inject
  DeviceProfileService deviceProfileService;

  @Override
  public List<DeviceProfileNote> getDeviceProfileNotes(String deviceId) {
    return deviceProfileService.getProfile(deviceId);
  }

  @Override
  public List<DeviceProfileNote> saveDeviceProfileNotes(
      Map<String, String> fields,
      String deviceId) {
    return deviceProfileService.saveProfile(deviceId, fields);
  }

  @Override
  public DeviceProfileNote addDeviceProfileNote(
      DeviceProfileNote field) {
    return deviceProfileService.createProfileField(field);
  }

  @Override
  public void deleteDeviceProfileNote(String deviceId, String keyName) {
    deviceProfileService.deleteProfileField(deviceId, keyName);
  }

  @Override
  public List<DeviceProfileFieldData> getFieldsData(String deviceId) {
    return deviceProfileService.getProfileFields(deviceId);
  }

  @Override
  public List<DeviceProfileFieldData> getAllDeviceData(String deviceId) {
    return deviceProfileService.getAllDeviceData(deviceId);
  }
}
