package esthesis.services.settings.impl;

import esthesis.service.settings.entity.DevicePageFieldEntity;
import esthesis.services.settings.impl.repository.SettingsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TestHelper {

	@Inject
	SettingsRepository settingsRepository;

	public void clearDatabase() {
		settingsRepository.deleteAll();
	}

	public DevicePageFieldEntity makeDevicePageFieldEntity(String name) {
		return new DevicePageFieldEntity(name, true, "label", "formatter", "icon");
	}
}
