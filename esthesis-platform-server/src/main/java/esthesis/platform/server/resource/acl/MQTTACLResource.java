package esthesis.platform.server.resource.acl;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.fuse.settings.service.SettingsService;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.extension.config.AppConstants.Generic;
import esthesis.platform.server.config.AppConstants.Setting;
import esthesis.platform.server.dto.acl.MosquittoAuthDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/acl/mqtt")
public class MQTTACLResource {

  private final SettingsService settingsService;

  public MQTTACLResource(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  private boolean isEnabled() {
    return settingsService.getSetting(Generic.SYSTEM, Setting.Mqtt.ACL_ENDPOINT_STATUS, Generic.SYSTEM)
        .getValAsBoolean();
  }

  @PostMapping(path = "/auth", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not check Mosquitto auth.")
  public ResponseEntity auth(MosquittoAuthDTO mosquittoACLDTO) {
    if (isEnabled()) {
      System.out.println(mosquittoACLDTO.toString());
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @PostMapping(path = "/acl", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not check Mosquitto auth.")
  public ResponseEntity acl(MosquittoAuthDTO mosquittoACLDTO) {
    if (isEnabled()) {
      System.out.println(mosquittoACLDTO.toString());
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @PostMapping(path = "/superuser", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not check Mosquitto auth.")
  public ResponseEntity superuser(MosquittoAuthDTO mosquittoACLDTO) {
    if (isEnabled()) {
      System.out.println(mosquittoACLDTO.toString());
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

}
