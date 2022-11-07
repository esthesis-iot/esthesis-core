package esthesis.services.settings.impl.service;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.settings.dto.Setting;
import esthesis.util.redis.EsthesisRedis;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SettingsService extends BaseService<Setting> {

  @Inject
  EsthesisRedis esthesisRedis;

  public Setting findByName(NamedSetting name) {
    log.debug("Looking up key '{}'.", name);
    Setting setting = findByColumn("name", name.toString());
    log.debug("Found value '{}'.", setting);

    return setting;
  }

  public Setting findByTextName(String name) {
    log.debug("Looking up key '{}'.", name);
    Setting setting = findByColumn("name", name);
    log.debug("Found value '{}'.", setting);

    return setting;
  }

  public List<String> findAllUniqueMeasurementNames() {
    return esthesisRedis.findAllUniqueMeasurementNames();
  }
}
