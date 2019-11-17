package esthesis.platform.server.resource.acl;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.common.config.AppConstants.Mqtt;
import esthesis.platform.server.config.AppSettings.Setting.Networking;
import esthesis.platform.server.config.AppSettings.Setting.Security;
import esthesis.platform.server.config.AppSettings.SettingValues.Networking.MqttAclEndpointStatus;
import esthesis.platform.server.dto.acl.MQTTAuthDTO;
import esthesis.platform.server.service.CertificatesService;
import esthesis.platform.server.service.SettingResolverService;
import javax.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log
@Validated
@RestController
@RequestMapping("/mqtt-acl")
public class MQTTACLResource {

  private final SettingResolverService settingsResolverService;
  private final CertificatesService certificatesService;
  private List<String> topics;

  public MQTTACLResource(SettingResolverService settingsResolverService,
    CertificatesService certificatesService) {
    this.settingsResolverService = settingsResolverService;
    this.certificatesService = certificatesService;

    // Get the list of available topics based on the supported MQTT events and construct valid topic
    // names.
    topics = Stream.of(Mqtt.EventType.class.getDeclaredFields())
      .filter(p -> !p.isSynthetic())
      .map(field -> {
        try {
          return "/" + field.get(null);
        } catch (IllegalAccessException e) {
          log.log(Level.SEVERE,
            MessageFormat.format("Could not get field value for field: {0}", field.getName()), e);
          return null;
        }
      }).collect(Collectors.toList());
  }

  @PostConstruct
  public void aVoid() {

  }

  private boolean isEnabled() {
    return settingsResolverService
      .is(Networking.MQTT_ACL_ENDPOINT_STATUS, MqttAclEndpointStatus.ACTIVE);
  }

  @PostMapping(path = "/auth", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not check for auth.")
  public ResponseEntity auth(MQTTAuthDTO mqttAuthDTO) {
    // Only certificate-based authentication is supported, so no support for account-based
    // authentication.
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  @PostMapping(path = "/acl", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not check for acl auth.")
  public ResponseEntity acl(MQTTAuthDTO mqttAuthDTO) {
    log.log(Level.FINEST, "ACL check for client {0} to topic {1}.", new Object[]{
      mqttAuthDTO.getTopic(), mqttAuthDTO.getUsername()});
    if (isEnabled()) {
      if (topics.stream()
        .anyMatch(p -> mqttAuthDTO.getTopic().equals(p + "/" + mqttAuthDTO.getUsername()))) {
        log.log(Level.FINEST, "\tALLOWED.");
        return ResponseEntity.ok().build();
      } else {
        log.log(Level.FINEST, "\tNOT ALLOWED.");
        return ResponseEntity.status(
          HttpStatus.NOT_FOUND).build();
      }
    } else {
      log.log(Level.FINEST, "\tACL checks not enabled.");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Check if the MQTT user is a superuser. As superuser is regarded any user which is authenticated
   * by the certificate selected in Settings > Security > Platform certificate.
   */
  @PostMapping(path = "/superuser", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not check for superuser.")
  public ResponseEntity superuser(MQTTAuthDTO mosquittoACLDTO) {
    log.log(Level.FINEST, "Superuser check for client {0}.",
      new Object[]{mosquittoACLDTO.getUsername()});
    if (isEnabled()) {
      if (mosquittoACLDTO.getUsername().equals(certificatesService
        .findEntityById(settingsResolverService.getAsLong(Security.PLATFORM_CERTIFICATE))
        .getCn())) {
        log.log(Level.FINEST, "\tALLOWED.");
        return ResponseEntity.ok().build();
      } else {
        log.log(Level.FINEST, "\tNOT ALLOWED.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
    } else {
      log.log(Level.FINEST, "\tSuperuser checks not enabled.");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

}
