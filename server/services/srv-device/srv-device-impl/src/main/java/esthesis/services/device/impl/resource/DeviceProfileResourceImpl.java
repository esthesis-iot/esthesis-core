package esthesis.services.device.impl.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
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
  public List<DeviceAttributeEntity> getDeviceProfileAttributes(String deviceId) {
    return deviceProfileService.getProfile(deviceId);
  }

  @Override
  @Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Save device profile attribute")
  public List<DeviceAttributeEntity> saveDeviceProfileAttributes(
      Map<String, String> fields,
      String deviceId) {
    return deviceProfileService.saveProfile(deviceId, fields);
  }

  @Override
  @Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Create device profile attribute")
  public DeviceAttributeEntity addDeviceProfileAttribute(
      DeviceAttributeEntity field) {
    return deviceProfileService.createProfileField(field);
  }

  @Override
  @Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Delete device profile attribute")
  public void deleteDeviceAttribute(String deviceId, String keyName) {
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
