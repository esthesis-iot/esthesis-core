package esthesis.services.device.impl.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.entity.DeviceProfileNoteEntity;
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
  public List<DeviceProfileNoteEntity> getDeviceProfileNotes(String deviceId) {
    return deviceProfileService.getProfile(deviceId);
  }

  @Override
  @Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Save device profile note")
  public List<DeviceProfileNoteEntity> saveDeviceProfileNotes(
      Map<String, String> fields,
      String deviceId) {
    return deviceProfileService.saveProfile(deviceId, fields);
  }

  @Override
  @Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Create device profile note")
  public DeviceProfileNoteEntity addDeviceProfileNote(
      DeviceProfileNoteEntity field) {
    return deviceProfileService.createProfileField(field);
  }

  @Override
  @Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Delete device profile note")
  public void deleteDeviceProfileNote(String deviceId, String keyName) {
    deviceProfileService.deleteProfileField(deviceId, keyName);
  }

  @Override
  public List<DeviceProfileFieldDataDTO> getFieldsData(String deviceId) {
    return deviceProfileService.getProfileFields(deviceId);
  }

  @Override
  public List<DeviceProfileFieldDataDTO> getAllDeviceData(String deviceId) {
    return deviceProfileService.getAllDeviceData(deviceId);
  }
}
