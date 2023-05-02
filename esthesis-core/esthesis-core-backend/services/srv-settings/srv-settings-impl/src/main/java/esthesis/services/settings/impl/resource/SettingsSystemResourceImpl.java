package esthesis.services.settings.impl.resource;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsSystemResource;
import esthesis.services.settings.impl.service.SettingsService;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SettingsSystemResourceImpl implements SettingsSystemResource {

	@Inject
	SettingsService settingsService;

	@Override
	public SettingEntity findByName(NamedSetting name) {
		return settingsService.findByName(name);
	}
}
