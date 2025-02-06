package esthesis.services.settings.impl.service;

import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.services.settings.impl.TestHelper;
import esthesis.util.redis.RedisUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@QuarkusTest
class SettingsServiceTest {

	@Inject
	SettingsService settingsService;

	@Inject
	TestHelper testHelper;

	@InjectMock
	RedisUtils redisUtils;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	void findByName() {
		// Assert no settings exist.
		for (NamedSetting setting : NamedSetting.values()) {
			assertNull(settingsService.findByName(setting));
		}

		// Perform the save operation for each setting.
		for (NamedSetting setting : NamedSetting.values()) {
			settingsService.saveNew(new SettingEntity(setting.name(), "test-value"));
		}

		// Assert all settings can be found.
		for (NamedSetting setting : NamedSetting.values()) {
			assertNotNull(settingsService.findByName(setting));
		}
	}

	@Test
	void findByTextName() {
		// Assert no settings exist.
		for (NamedSetting setting : NamedSetting.values()) {
			assertNull(settingsService.findByTextName(setting.name()));
		}

		// Perform the save operation for each setting.
		for (NamedSetting setting : NamedSetting.values()) {
			settingsService.saveNew(new SettingEntity(setting.name(), "test-value"));
		}

		// Assert all settings can be found.
		for (NamedSetting setting : NamedSetting.values()) {
			assertNotNull(settingsService.findByTextName(setting.name()));
		}

	}

	@Test
	void findAllUniqueMeasurementNames() {
		// Mock retrieving measurements from redis.
		when(redisUtils.findKeysStartingWith(RedisUtils.KeyType.ESTHESIS_DM.toString()))
			.thenReturn(List.of("category.measurement1", "category.measurement2", "category.measurement3"));

		when(redisUtils.getHash("category.measurement1"))
			.thenReturn(Map.of("category.measurement1", "test-value1"));
		when(redisUtils.getHash("category.measurement2"))
			.thenReturn(Map.of("category.measurement2", "test-value2"));
		when(redisUtils.getHash("category.measurement3"))
			.thenReturn(Map.of("category.measurement3", "test-value3"));

		// Assert can find unique measurements.
		assertFalse(settingsService.findAllUniqueMeasurementNames().isEmpty());

	}

	@Test
	void saveNew() {
		// Perform the save operation.
		settingsService.saveNew(new SettingEntity("new setting", "test-value"));

		// Assert the setting was saved with the correct value.
		assertEquals("test-value", settingsService.findByTextName("new setting").getValue());
	}

	@Test
	void saveUpdate() {
		// Perform the save operation.
		settingsService.saveNew(new SettingEntity("new setting", "test-value"));

		// Find the setting and perform the update operation.
		SettingEntity settingEntity = settingsService.findByTextName("new setting");
		settingEntity.setValue("updated-value");
		settingsService.saveUpdate(settingEntity);

		// Assert the setting was updated with the correct value.
		assertEquals("updated-value", settingsService.findByTextName("new setting").getValue());
	}
}
