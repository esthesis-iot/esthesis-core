package esthesis.services.device.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DeviceProfileFieldData;
import esthesis.service.device.dto.DeviceProfileNote;
import esthesis.service.settings.dto.DevicePageField;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.services.device.impl.repository.DeviceProfileFieldRepository;
import esthesis.util.redis.EsthesisRedis;
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
public class DeviceProfileService extends BaseService<DeviceProfileNote> {

  @Inject
  DeviceProfileFieldRepository deviceProfileFieldRepository;

  @Inject
  @RestClient
  SettingsResource settingsResource;

  @Inject
  DeviceService deviceService;

  @Inject
  EsthesisRedis redis;

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

  public List<DeviceProfileFieldData> getProfileFields(String deviceId) {
    List<DeviceProfileFieldData> fields = new ArrayList<>();

    // Get configured device profile fields data.
    List<DevicePageField> devicePageFields = settingsResource.getDevicePageFields();

    // Find the value of each field.
    Device device = deviceService.findById(deviceId);
    devicePageFields.stream().filter(DevicePageField::isShown)
        .forEach(field -> {
          DeviceProfileFieldData deviceProfileFieldData = new DeviceProfileFieldData();
          deviceProfileFieldData
              .setLabel(field.getLabel())
              .setIcon(field.getIcon())
              .setValueType(redis.getValueType(device.getHardwareId(),
                  field.getMeasurement()))
              .setLastUpdate(
                  redis.getLastUpdate(device.getHardwareId(),
                      field.getMeasurement()));

          String value = redis.getValue(device.getHardwareId(),
              field.getMeasurement());
          if (StringUtils.isNotEmpty(field.getFormatter())) {
            deviceProfileFieldData.setValue(Qute.fmt(field.getFormatter()).data(
                "val", value).render());
          } else {
            deviceProfileFieldData.setValue(value);
          }

          fields.add(deviceProfileFieldData);
        });

    return fields;
  }

  public List<DeviceProfileFieldData> getAllDeviceData(String deviceId) {
    List<DeviceProfileFieldData> fields = new ArrayList<>();
    Device device = deviceService.findById(deviceId);

    redis.getAllForKey(device.getHardwareId()).forEach((triple) -> {
      DeviceProfileFieldData deviceProfileFieldData = new DeviceProfileFieldData();
      deviceProfileFieldData
          .setLabel(triple.getLeft())
          .setValue(triple.getMiddle())
          .setLastUpdate(triple.getRight());
      fields.add(deviceProfileFieldData);
    });

    return fields;
  }
}
