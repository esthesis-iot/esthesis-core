package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.common.util.KeyValue;
import com.eurodyn.qlack.fuse.settings.dto.SettingDTO;
import com.eurodyn.qlack.fuse.settings.service.SettingsService;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import esthesis.common.config.AppConstants.Generic;
import javax.validation.Valid;
import javax.ws.rs.core.Response;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not find setting.")
  public SettingDTO findByName(@RequestParam String name) {
    return settingsService.getSetting(Generic.SYSTEM, name, Generic.SYSTEM);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save setting.")
  public void save(@Valid @RequestBody SettingDTO settingDTO) {
    settingsService
      .setVal(Generic.SYSTEM, settingDTO.getKey(), settingDTO.getVal(), Generic.SYSTEM);
  }

  @GetMapping("byNames")
  @ReplyFilter("key,val")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not find settings.")
  public List<SettingDTO> findByNames(@RequestParam String names) {
    return settingsService
      .getSettings(Generic.SYSTEM, Arrays.asList(names.split(",")), Generic.SYSTEM);
  }

  @PostMapping("byNames")
  @ReplyFilter("key,val")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save settings.")
  public Response saveMultiple(@Valid @RequestBody List<KeyValue> settings) {
    settingsService.setVals(Generic.SYSTEM,
      settings.stream().map(keyValue -> keyValue.getKey().toString()).collect(Collectors.toList()),
      settings.stream().map(keyValue -> keyValue.getValue() != null ?
        keyValue.getValue().toString() : "").collect(Collectors.toList()),
      Generic.SYSTEM);

    return Response.ok().build();
  }

}
