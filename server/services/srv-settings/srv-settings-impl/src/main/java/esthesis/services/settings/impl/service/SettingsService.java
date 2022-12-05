package esthesis.services.settings.impl.service;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.settings.dto.Setting;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SettingsService extends BaseService<Setting> {

  @Inject
  RedisUtils redisUtils;

  public Setting findByName(NamedSetting name) {
    log.debug("Looking up key '{}'.", name);
    Setting setting = findFirstByColumn("name", name.toString());
    log.debug("Found value '{}'.", setting);

    return setting;
  }

  public Setting findByTextName(String name) {
    log.debug("Looking up key '{}'.", name);
    Setting setting = findFirstByColumn("name", name);
    log.debug("Found value '{}'.", setting);

    return setting;
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
