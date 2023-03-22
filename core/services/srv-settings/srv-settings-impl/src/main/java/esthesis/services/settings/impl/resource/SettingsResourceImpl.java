package esthesis.services.settings.impl.resource;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.settings.entity.DevicePageFieldEntity;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.services.settings.impl.service.DevicePageFieldService;
import esthesis.services.settings.impl.service.SettingsService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
public class SettingsResourceImpl implements SettingsResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  SettingsService settingsService;

  @Inject
  DevicePageFieldService devicePageFieldService;

  @Override
  @Audited(cat = Category.SETTINGS, op = Operation.READ, msg = "Get setting")
  public SettingEntity findByName(NamedSetting name) {
    return settingsService.findByName(name);
  }

  @Override
  @Audited(cat = Category.SETTINGS, op = Operation.READ, msg = "Get settings")
  public List<SettingEntity> findByNames(String names) {
    return Arrays.stream(names.split(",")).map(
            name -> settingsService.findByName(NamedSetting.valueOf(name)))
        .filter(Objects::nonNull).toList();
  }

  @Override
  @Audited(cat = Category.SETTINGS, op = Operation.WRITE, msg = "Save settings")
  public void save(SettingEntity... settingEntities) {
    // Saving a setting entry is a special case as the caller might want to
    // overwrite the value of a setting entry by name (i.e. without knowing
    // the setting entry id).
    for (SettingEntity entry : settingEntities) {
      if (entry.getId() != null) {
        log.debug("Updating an existing setting entry by id with '{}'.",
            entry);
        settingsService.save(entry);
      } else {
        SettingEntity existingEntry = settingsService.findByTextName(
            entry.getName());
        if (existingEntry != null) {
          log.debug("Updating an existing setting entry with '{}'.", entry);
          entry.setId(existingEntry.getId());
          settingsService.save(entry);
        } else {
          log.debug("Creating a new setting entry '{}'.", entry);
          settingsService.save(entry);
        }
      }
    }
  }

  @Override
  public List<String> findAllUniqueMeasurementNames() {
    return settingsService.findAllUniqueMeasurementNames();
  }

  @Override
  public List<DevicePageFieldEntity> getDevicePageFields() {
    return devicePageFieldService.getFields();
  }

  @Override
  @Audited(cat = Category.SETTINGS, op = Operation.WRITE, msg = "Save device page fields")
  public void saveDevicePageFields(@Valid List<DevicePageFieldEntity> fields) {
    devicePageFieldService.saveFields(fields);
  }
}
