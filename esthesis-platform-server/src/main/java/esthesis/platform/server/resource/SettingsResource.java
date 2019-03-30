package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.fuse.settings.dto.SettingDTO;
import com.eurodyn.qlack.fuse.settings.service.SettingsService;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import esthesis.platform.common.config.AppConstants.Generic;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/settings")
public class SettingsResource {

  private final SettingsService settingsService;

  public SettingsResource(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @GetMapping("byName")
  @ReplyFilter("key,val")
  public SettingDTO findByName(@RequestParam String name) {
    return settingsService.getSetting(Generic.SYSTEM, name, Generic.SYSTEM);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save setting.")
  public void save(@Valid @RequestBody SettingDTO settingDTO) {
    settingsService.setVal(Generic.SYSTEM, settingDTO.getKey(), settingDTO.getVal(), Generic.SYSTEM);
  }
}
