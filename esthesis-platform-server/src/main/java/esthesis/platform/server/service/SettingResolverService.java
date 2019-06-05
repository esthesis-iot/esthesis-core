package esthesis.platform.server.service;

import com.eurodyn.qlack.fuse.settings.service.SettingsService;
import esthesis.extension.common.config.AppConstants.Generic;
import org.springframework.stereotype.Service;

@Service
public class SettingResolverService {

  private final SettingsService settingsService;

  public SettingResolverService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  public String get(String settingName) {
    return settingsService.getSetting(Generic.SYSTEM, settingName, Generic.SYSTEM).getVal();
  }

  public long getAsLong(String settingName) {
    return Long.parseLong(get(settingName));
  }

  public int getAsInt(String settingName) {
    return Integer.parseInt(get(settingName));
  }

  public boolean is(String settingName, String value) {
    return get(settingName).equals(value);
  }

  public boolean isNot(String settingName, String value) {
    return !get(settingName).equals(value);
  }

  public boolean isAny(String settingName, String... values) {
    boolean isAny = false;

    for (String value : values) {
      isAny = isAny || get(settingName).equals(value);
      if (isAny) {
        break;
      }
    }

    return isAny;
  }

  public boolean isNotAny(String settingName, String... values) {
    boolean isAny = true;

    for (String value : values) {
      isAny = isAny && get(settingName).equals(value);
      if (isAny) {
        break;
      }
    }

    return isAny;
  }
}
