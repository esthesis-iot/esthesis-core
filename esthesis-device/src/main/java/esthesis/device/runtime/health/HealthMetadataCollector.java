package esthesis.device.runtime.health;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.device.runtime.config.AppConstants.Mqtt;
import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.mqtt.MqttClient;
import esthesis.device.runtime.service.RegistrationService;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log
@Component
public class HealthMetadataCollector {

  private final MqttClient mqttClient;
  private final AppProperties appProperties;
  private final ObjectMapper objectMapper;
  private final RegistrationService registrationService;
  private static final Instant STARTUP_TIME = Instant.now();
  // An internal flag to indicate that hardware information can not be obtained for the platform on which the device
  // agent run on. This flag is automatically set when a java.lang.UnsatisfiedLinkError is thrown, so to not polute the
  // log output with continous error messages.
  public static boolean hwPlatformError = false;

  public HealthMetadataCollector(MqttClient mqttClient,
    AppProperties appProperties, ObjectMapper objectMapper,
    RegistrationService registrationService) {
    this.mqttClient = mqttClient;
    this.appProperties = appProperties;
    this.objectMapper = objectMapper;
    this.registrationService = registrationService;
  }

  @Async
  @Scheduled(fixedRateString = "${pingFreqMsec:60000}",
    initialDelayString = "${pingInitialDelayMsec:60000}")
  public void pingScheduler() {
    if (StringUtils.isNotEmpty(registrationService.getMqttServer())) {
      mqttClient.publish(Mqtt.EventType.PING, ping());
    }
  }

  public byte[] ping() {
    try {
      return objectMapper.writeValueAsBytes(
        new DevicePingMessageDTO().setDeviceTime(Instant.now().toEpochMilli()));
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not produce JSON output for ping data.", e);
      return new byte[0];
    }
  }

  @Async
  @Scheduled(fixedRateString = "${healthDataFreqMsec:900000}",
    initialDelayString = "${healthDataInitialDelayMsec:300000}")
  public void collectHealthDataScheduler() {
    if (StringUtils.isNotEmpty(registrationService.getMqttServer())) {
      byte[] healthData = collectHealthData();
      if (ArrayUtils.isNotEmpty(healthData)) {
        mqttClient.publish(Mqtt.EventType.TELEMETRY, collectHealthData());
      }
    }
  }

  @SuppressWarnings("java:S3776")
  public byte[] collectHealthData() {
    if (hwPlatformError) {
      return null;
    }

    try {
      DeviceHealthDataDTO deviceHealthDataDTO = new DeviceHealthDataDTO();

      // Time.
      if (appProperties.isHcCurrentTime()) {
        deviceHealthDataDTO.setCurrentTime(Instant.now().toEpochMilli());
      }
      if (appProperties.isHcUpTime()) {
        deviceHealthDataDTO.setUpTime(ChronoUnit.MILLIS.between(STARTUP_TIME, Instant.now()));
      }

      // IP Address.
      if (appProperties.isHcIpAddress()) {
        deviceHealthDataDTO
          .setIpAddress(IPHelper.getIPAddress(appProperties.getHcIpIfFilter()));
      }

      // Runtime agent version information.
      if (appProperties.isRuntimeVersion() || appProperties.isRuntimeCommitId()) {
        // Get versioning info.
        VersionDTO versionDTO = objectMapper
          .readValue(this.getClass().getResourceAsStream("/git.json"), VersionDTO.class);
        if (appProperties.isRuntimeVersion()) {
          deviceHealthDataDTO.setRuntimeVersion(versionDTO.getBuildVersion());
        }
        if (appProperties.isRuntimeCommitId()) {
          deviceHealthDataDTO.setRuntimeCommitId(versionDTO.getCommitId());
        }
      }

      // Firmware version information.
      if (StringUtils.isNotEmpty(appProperties.getFirmwareVersionFile())) {
        deviceHealthDataDTO.setFirmwareVersion(FileUtils
          .readFileToString(new File(appProperties.getFirmwareVersionFile()),
            StandardCharsets.UTF_8));
      }

      // Create a JSON representation of the health data.
      DeviceHealthMessageDTO deviceHealthDTO = new DeviceHealthMessageDTO();
      deviceHealthDTO.setDeviceHealthDataDTO(deviceHealthDataDTO);
      JsonNode node = objectMapper.valueToTree(deviceHealthDTO);

      return objectMapper.writeValueAsBytes(node);
    } catch (java.lang.UnsatisfiedLinkError e) {
      hwPlatformError = true;
      log.log(Level.SEVERE,
        "Could not obtain hardware information. Future health checks will be ignored.", e);
      return null;
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not obtain health data.", e);
      return null;
    }
  }
}
