package esthesis.platform.server.resource.acl;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.platform.server.config.AppSettings.Setting.Networking;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.config.AppSettings.SettingValues.Networking.MqttAclEndpointStatus;
import esthesis.platform.server.dto.acl.MosquittoAuthDTO;
import esthesis.platform.server.service.CertificatesService;
import esthesis.platform.server.service.SettingResolverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/mqtt-acl")
public class MQTTACLResource {

  private final SettingResolverService settingsResolverService;
  private final CertificatesService certificatesService;

  public MQTTACLResource(SettingResolverService settingsResolverService,
    CertificatesService certificatesService) {
    this.settingsResolverService = settingsResolverService;
    this.certificatesService = certificatesService;
  }

  private boolean isEnabled() {
    return settingsResolverService
      .is(Networking.MQTT_ACL_ENDPOINT_STATUS, MqttAclEndpointStatus.ACTIVE);
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

  /**
   * Check if the MQTT user is a superuser. As superuser is regarded any user which is authenticated
   * by the certificate selected in Settings > Security > Platform certificate.
   */
  @PostMapping(path = "/superuser", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not check Mosquitto auth.")
  public ResponseEntity superuser(MosquittoAuthDTO mosquittoACLDTO) {
    if (isEnabled()) {
      if (mosquittoACLDTO.getUsername().equals(certificatesService
        .findEntityById(settingsResolverService.getAsLong(Security.PLATFORM_CERTIFICATE))
        .getCn())) {
        return ResponseEntity.ok().build();
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

}
