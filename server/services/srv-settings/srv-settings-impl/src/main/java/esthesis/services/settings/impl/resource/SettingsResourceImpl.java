package esthesis.services.settings.impl.resource;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.settings.dto.DevicePageField;
import esthesis.service.settings.dto.Setting;
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
  public Setting findByName(NamedSetting name) {
    return settingsService.findByName(name);
  }

  @Override
  public List<Setting> findByNames(String names) {
    return Arrays.stream(names.split(",")).map(
            name -> settingsService.findByName(NamedSetting.valueOf(name)))
        .filter(Objects::nonNull).toList();
  }

  @Override
  public void save(Setting... settings) {
    // Saving a setting entry is a special case as the caller might want to
    // overwrite the value of a setting entry by name (i.e. without knowing
    // the setting entry id).
    for (Setting entry : settings) {
      if (entry.getId() != null) {
        log.debug("Updating an existing setting entry by id with '{}'.",
            entry);
        settingsService.save(entry);
      } else {
        Setting existingEntry = settingsService.findByTextName(
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
  public List<DevicePageField> getDevicePageFields() {
    return devicePageFieldService.getFields();
  }

  @Override
  public void saveDevicePageFields(@Valid List<DevicePageField> fields) {
    devicePageFieldService.saveFields(fields);
  }
}
