package esthesis.services.settings.impl.service;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SettingsService extends BaseService<SettingEntity> {

	@Inject
	RedisUtils redisUtils;

	public SettingEntity findByName(NamedSetting name) {
		log.debug("Looking up key '{}'.", name);
		SettingEntity settingEntity = findFirstByColumn("name", name.toString());
		log.debug("Found value '{}'.", settingEntity);

		return settingEntity;
	}

	public SettingEntity findByTextName(String name) {
		log.debug("Looking up key '{}'.", name);
		SettingEntity settingEntity = findFirstByColumn("name", name);
		log.debug("Found value '{}'.", settingEntity);

		return settingEntity;
	}

	/**
	 * Finds all unique measurement names sent by devices.
	 * <p>
	 * TODO: Evaluate performance with a large number of devices in Redis.
	 */
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
}
