package esthesis.device.runtime.config;

import esthesis.device.runtime.config.AppConstants.ExitCode;
import esthesis.device.runtime.resolver.id.HardwareIdResolverUtil;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

@Log
@Data
@Component
@Configuration
public class AppProperties {

  // The URL of the registration server.
  @Value("${registrationUrl:}")
  private String registrationUrl;

  // The registration ID that uniquely identifies this device.
  @Value("${hardwareId}")
  private String hardwareId;

  // A comma-separated list of tags for the device to present during registration.
  @Value("${tags:}")
  private String tags;

  // The root folder under which persistent storage is provided.
  @Value("${storageRoot}")
  private String storageRoot;

  // The root folder under which secure persistent storage is provided. If this is left empty,
  // storageRoot is used.
  @Value("${secureStorageRoot:${storageRoot}}")
  private String secureStorageRoot;

  // The root folder to store remotely retrieved provisioning packages.
  @Value("${provisioningRoot:${storageRoot}/provisioning}")
  private String provisioningRoot;

  // The root folder to temporarily download a remotely retrieved provisioning packages. Once the
  // package is downloaded, it is moved to provisioningRoot.
  @Value("${provisioningTempRoot:${storageRoot}/provisioning/.tmp}")
  private String provisioningTempRoot;

  // The maximum number a request (to esthesis platform) is retried.
  @Value("${device.runtime.request.attempts:100}")
  private int requestAttempts;

  // Number of milliseconds to wait before trying again a previously failed request.
  @Value("${device.runtime.request.backoff:1000}")
  private int requestRetryBackoff;

  // The maximum number of minutes to wait between attempts of previously failed requests.
  @Value("${device.runtime.request.backoff.max:60}")
  private int requestMaxBackoff;

  // The algorithm the asymmetric keys (i.e. public and private keys) are created with.
  @Value("${asymmetricKeyAlgorithm:RSA}")
  private String asymmetricKeyAlgorithm;

  // A flag for the device to skip initial provisioning. This is useful in case the device comes
  // with a firmware image already installed during factory setup.
  @Value("${skipInitialProvisioning:true}")
  private boolean skipInitialProvisioning;

  // A provisioning package contains a script that will be executed by the agent in order to
  // initiate the actual provisioning process. This flag defines how such execution will take place:
  // soft: The script is called as a child process, controlled by the runtime agent. As soon as the
  //       agent terminates, the provisioning script terminates too.
  // hard: The script is called as an independent process, not controlled by the runtime agent.
  @Value("${provisioningForkType:soft}")
  private String provisioningForkType;

  // A flag indicating to skip the initial device registration.
  @Value("${skipRegistration:false}")
  private boolean skipRegistration;

  // A flag indicating that startup is paused until a key is pressed.
  @Value("${pauseStartup:false}")
  private boolean pauseStartup;

  // The script to be called after a provisioning package is downloaded.
  // The script is handed the following parameters:
  // 1. The full pathname to the provisioning package.
  // 2. Whether this is an initial provisioning or not (as a true/false value).
  @Value("${provisioningPostHook:}")
  private String provisioningPostHook;

  // A flag to indicate that the embedded MQTT-to-MQTT proxy server should be started.
  @Value("${proxyMqtt:false}")
  private boolean proxyMqtt;

  // The port of the embedded proxy MQTT server.
  @Value("${proxyMqttPort:4566}")
  private int proxyMqttPort;

  // A flag to indicate that the embedded web-to-MQTT proxy server should be started.
  @Value("${proxyWeb:false}")
  private boolean proxyWeb;

  // The port of the embedded proxy Web server.
  @Value("${proxyWebPort:4567}")
  private int proxyWebPort;

  // How often health data from the node are transmitted back to the platform (in msec).
  // See also HealthMetadataCollection#collectHealthData.
  @Value("${healthDataFreqMsec:3600000}")
  private long healthDataFreqMsec;

  // How long to wait before starting transmitting health data.
  // See also HealthMetadataCollection#collectHealthData.
  @Value("${healthDataInitialDelayMsec:300000}")
  private long healthDataInitialDelayMsec;

  // How often ping data is sent.
  // See also HealthMetadataCollection#ping.
  @Value("${pingFreqMsec:60000}")
  private long pingFreqMsec;

  // How long to wait before start sending ping data.
  // See also HealthMetadataCollection#ping.
  @Value("${pingInitialDelayMsec:60000}")
  private long pingInitialDelayMsec;

  // Return the device's local clock date/time.
  @Value("${hcCurrentTime:true}")
  private boolean hcCurrentTime;

  // Returns the device's uptime in health messages.
  @Value("${hcUpTime:true}")
  private boolean hcUpTime;

  // Returns the device's IP address in health messages.
  @Value("${hcIpAddress:true}")
  private boolean hcIpAddress;

  // A comma-separated list of interface names to include when reporting their IP address. If left
  // empty, all interfaces will be included.
  @Value("${hcIpIfFilter:}")
  private String hcIpIfFilter;

  // Returns the device's agent version in health messages.
  @Value("${runtimeVersion:true}")
  private boolean runtimeVersion;

  // Returns the commit ID of the runtime agent running on the device in health messages.
  @Value("${runtimeCommitId:true}")
  private boolean runtimeCommitId;

  // The file containing the firmware version to be reported.
  @Value("${firmwareVersionFile:}")
  private String firmwareVersionFile;

  // The command to reboot the device.
  @Value("${rebootCommand:reboot}")
  private String rebootCommand;

  // The command to shutdown the device.
  @Value("${shutdownCommand:shutdown}")
  private String shutdownCommand;

  // A comma-separated list of the commands this device supports.
  @Value("${supportedCommands:PROVISIONING_CHECK_NEW,PING,HEALTH,REBOOT,EXECUTE,SHUTDOWN}")
  private String supportedCommands;

  // The MQTT topic to send PING messages.
  @Value("${topicPing:esthesis/ping}")
  private String topicPing;

  // The MQTT topic to send TELEMETRY messages.
  @Value("${topicTelemetry:esthesis/telemetry}")
  private String topicTelemetry;

  // The MQTT topic to send METADATA messages.
  @Value("${topicMetadata:esthesis/metadata}")
  private String topicMetadata;

  // The MQTT topic to listen for CONTROL REQUEST messages.
  @Value("${topicControlRequest:esthesis/control/request}")
  private String topicControlRequest;

  // The MQTT topic to send CONTROL REPLY messages.
  @Value("${topicControlReply:esthesis/control/reply}")
  private String topicControlReply;

  public String getHardwareId() {
    try {
      return HardwareIdResolverUtil.resolve(hardwareId);
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
      IOException | InvocationTargetException e) {
      log.log(Level.SEVERE, "Could not obtain device ID.", e);
      System.exit(ExitCode.CANNOT_FIND_DEVICE_ID);
      return null;
    }
  }

  // A flag indicating this device should send demo data.
  @Value("${demo:false}")
  private boolean demo;

  // The frequency (in msec) in which demo data is transmitted.
  @Value("${demoFreqMsec:5000}")
  private long demoFreqMsec;

  @Value("${demoInitialDelayMsec:5000}")
  private long demoInitialDelayMsec;

  // The JSON payload to send as demo content.
  // Variable substitution:
  //    %i%: Random integer 0-100
  //    %f%: Random float 0-100.
  @Value("${demoPayload:{\"m\": \"demo\", \"v\": { \"temperature\": %i%, \"humidity\": %f%}}}")
  private String demoPayload;

}


