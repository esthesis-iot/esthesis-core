package esthesis.services.settings.impl.service;

import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.SETTINGS;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing settings.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class SettingsService extends BaseService<SettingEntity> {

	@Inject
	RedisUtils redisUtils;

	/**
	 * Save handler for settings.
	 *
	 * @param entity the setting to save.
	 * @return the saved entity.
	 */
	private SettingEntity saveHandler(SettingEntity entity) {
		return super.save(entity);
	}

	/**
	 * Find setting by name.
	 *
	 * @param name the setting name.
	 * @return the setting entity.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = SETTINGS, operation = READ)
	public SettingEntity findByName(NamedSetting name) {
		log.trace("Looking up key '{}'.", name);
		SettingEntity settingEntity = findFirstByColumn("name", name.toString());
		log.trace("Found value '{}'.", settingEntity);

		return settingEntity;
	}

	/**
	 * Find setting by the textual representation of its name.
	 *
	 * @param name the setting name.
	 * @return the setting entity.
	 */
	@ErnPermission(category = SETTINGS, operation = READ)
	public SettingEntity findByTextName(String name) {
		log.trace("Looking up key '{}'.", name);
		SettingEntity settingEntity = findFirstByColumn("name", name);
		log.trace("Found value '{}'.", settingEntity);

		return settingEntity;
	}

	/**
	 * Finds all unique measurement names sent by devices.
	 *
	 * @return the list of unique measurement names.
	 */
	@ErnPermission(category = SETTINGS, operation = READ)
	public List<String> findAllUniqueMeasurementNames() {
		// Get all hash names containing measurement values.
		List<String> keys = redisUtils.findKeysStartingWith(KeyType.ESTHESIS_DM.toString());

		// For each hash get the measurement names and add them to a unique list.
		Set<String> fields = new TreeSet<>();
		for (String key : keys) {
			Map<String, String> hash = redisUtils.getHash(key);
			fields.addAll(hash.keySet().stream().filter(
				s -> !s.endsWith("." + AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP) && !s.endsWith(
					"." + AppConstants.REDIS_KEY_SUFFIX_VALUE_TYPE)).toList());
		}

		return fields.stream().sorted().toList();
	}

	/**
	 * Create a new setting.
	 *
	 * @param entity the setting to create.
	 * @return the created entity.
	 */
	@ErnPermission(category = SETTINGS, operation = CREATE)
	public SettingEntity saveNew(SettingEntity entity) {
		return saveHandler(entity);
	}

	/**
	 * Save or update a setting.
	 *
	 * @param entity the setting to save.
	 * @return the saved entity.
	 */
	@ErnPermission(category = SETTINGS, operation = WRITE)
	public SettingEntity saveUpdate(SettingEntity entity) {
		return saveHandler(entity);
	}
}
