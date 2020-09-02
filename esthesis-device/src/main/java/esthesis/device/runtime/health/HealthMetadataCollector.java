package esthesis.device.runtime.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import esthesis.device.runtime.config.AppConstants.Mqtt;
import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.mqtt.MqttClient;
import esthesis.device.runtime.service.RegistrationService;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;

@Log
@Component
public class HealthMetadataCollector {

  private final MqttClient mqttClient;
  private final AppProperties appProperties;
  private final ObjectMapper objectMapper;
  private final RegistrationService registrationService;
  private final static Instant startupTime = Instant.now();

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
  public void ping() {
    if (StringUtils.isNotEmpty(registrationService.getEmbeddedMqttServer())) {
      try {
        mqttClient.publish(Mqtt.EventType.PING, objectMapper
          .writeValueAsBytes(
            new DevicePingMessageDTO().setDeviceTime(Instant.now().toEpochMilli())));
      } catch (Exception e) {
        log.log(Level.SEVERE, "Could not produce JSON output for ping data.", e);
      }
    }
  }

  @Async
  @Scheduled(fixedRateString = "${healthDataFreqMsec:900000}",
    initialDelayString = "${healthDataInitialDelayMsec:300000}")
  public void collectHealthData() {
    if (StringUtils.isNotEmpty(registrationService.getEmbeddedMqttServer())) {
      try {
        DeviceHealthDataDTO deviceHealthDataDTO = new DeviceHealthDataDTO();

        SystemInfo si = new SystemInfo();

        // OS details.
        final OperatingSystem operatingSystem = si.getOperatingSystem();

        // CPU.
        HardwareAbstractionLayer hal = si.getHardware();
        final CentralProcessor processor = hal.getProcessor();
        final Sensors sensors = hal.getSensors();
        if (appProperties.isHcCpuTemperature()) {
          deviceHealthDataDTO.setCpuTemperature(sensors.getCpuTemperature());
        }

        // Memory.
        final GlobalMemory memory = hal.getMemory();
        if (appProperties.isHcMemoryAvailable()) {
          deviceHealthDataDTO.setMemoryAvailable(memory.getAvailable());
        }
        if (appProperties.isHcMemoryTotal()) {
          deviceHealthDataDTO.setMemoryTotal(memory.getTotal());
        }

        // Load.
        double[] loadAverage = processor.getSystemLoadAverage(3);
        if (appProperties.isHcLoad1()) {
          deviceHealthDataDTO.setLoad1(loadAverage[0] < 0 ? 0 : loadAverage[0]);
        }
        if (appProperties.isHcLoad5()) {
          deviceHealthDataDTO.setLoad5(loadAverage[1] < 0 ? 0 : loadAverage[1]);
        }
        if (appProperties.isHcLoad15()) {
          deviceHealthDataDTO.setLoad15(loadAverage[2] < 0 ? 0 : loadAverage[2]);
        }

        // Time.
        if (appProperties.isHcCurrentTime()) {
          deviceHealthDataDTO.setCurrentTime(Instant.now().toEpochMilli());
        }
        if (appProperties.isHcUpTime()) {
          deviceHealthDataDTO.setUpTime(ChronoUnit.MILLIS.between(startupTime, Instant.now()));
        }

        // IP Address.
        if (appProperties.isHcIpAddress()) {
          deviceHealthDataDTO
            .setIpAddress(IPHelper.getIPAddress(Optional.of(appProperties.getHcIpIfFilter())));
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

        // Add additional nodes to JSON for the filesystems.
        if (appProperties.isHcFs()) {
          JsonNode valuesRoot = node.get("v");
          final FileSystem fileSystem = operatingSystem.getFileSystem();
          OSFileStore[] fsArray = fileSystem.getFileStores();
          for (OSFileStore fs : fsArray) {
            if (StringUtils.isEmpty(appProperties.getHcFilterFs()) ||
              Arrays.stream(appProperties.getHcFilterFs().split(","))
                .anyMatch(f -> f.equals(fs.getMount()))) {
              ((ObjectNode) valuesRoot).put("mount_free_" + fs.getMount(), fs.getUsableSpace());
              ((ObjectNode) valuesRoot).put("mount_total_" + fs.getMount(), fs.getTotalSpace());
            }
          }
        }

        try {
          mqttClient.publish(Mqtt.EventType.TELEMETRY, objectMapper.writeValueAsBytes(node));
        } catch (JsonProcessingException e) {
          log.log(Level.SEVERE, "Could not produce JSON output for health data.", e);
        }
      } catch (Exception e) {
        log.log(Level.SEVERE, "Could not obtain health data.", e);
      }
    }
  }
}
