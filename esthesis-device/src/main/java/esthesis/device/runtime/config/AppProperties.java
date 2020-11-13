package esthesis.device.runtime.config;

import esthesis.device.runtime.config.AppConstants.ExitCode;
import esthesis.device.runtime.resolver.id.HardwareIdResolverUtil;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

@Data
@Component
@Log
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

  // Whether outgoing messages are encrypted.
  @Value("${outgoingEncrypted:false}")
  private boolean outgoingEncrypted;

  // Whether outgoing messages are signed.
  @Value("${outgoingSigned:false}")
  private boolean outgoingSigned;

  // Whether incoming messages should be encrypted.
  @Value("${incomingEncrypted:false}")
  private boolean incomingEncrypted;

  // Whether incoming messages should be signed.
  @Value("${incomingSigned:false}")
  private boolean incomingSigned;

  // Whether incoming provisioning packages should be encrypted.
  @Value("${provisioningEncrypted:false}")
  private boolean provisioningEncrypted;

  // Whether incoming provisioning packages should be signed.
  @Value("${provisioningSigned:false}")
  private boolean provisioningSigned;

  // The cipher used for symmetric encryption/decryption.
  @Value("${symmetricCipher:AES/CBC/PKCS5Padding}")
  private String symmetricCipher;

  // The cipher used for asymmetric encryption/decryption.
  @Value("${asymmetricCipher:RSA/ECB/PKCS1Padding}")
  private String asymmetricCipher;

  // The algorithm the symmetric key (i.e. the session key) is created with.
  @Value("${symmetricKeyAlgorithm:AES}")
  private String symmetricKeyAlgorithm;

  // The algorithm the asymmetric keys (i.e. public and private keys) are created with.
  @Value("${asymmetricKeyAlgorithm:RSA}")
  private String asymmetricKeyAlgorithm;

  // The algorithm to be used when signing messages.
  @Value("${signatureAlgorithm:SHA256withRSA}")
  private String signatureAlgorithm;

  // A flag for the device to skip initial provisioning. This is useful in case the device comes
  // with a firmware image already installed during factory setup.
  @Value("${skipInitialProvisioning:false}")
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

  // Return manufacturer information in health messages.
  @Value("${hcOsManufacturer:true}")
  private boolean hcOsManufacturer;

  // Return OS version information in health messages.
  @Value("${hcOsVersion:true}")
  private boolean hcOsVersion;

  // Return hardware serial number information in health messages.
  @Value("${hcHwSerial:true}")
  private boolean hcHwSerial;

  // Return the number of CPUs information in health messages.
  @Value("${hcCpuPhysicalPackage:true}")
  private boolean hcCpuPhysicalPackage;

  // Return the number of physical CPU cores information in health messages.
  @Value("${hcCpuPhysicalCores:true}")
  private boolean hcCpuPhysicalCores;

  // Return the number of logical CPU cores information in health messages.
  @Value("${hcCpuLogicalCores:true}")
  private boolean hcCpuLogicalCores;

  // Return the CPU identifier information in health messages.
  @Value("${hcCpuIdentifier:true}")
  private boolean hcCpuIdentifier;

  // Return the CPU processor ID information in health messages.
  @Value("${hcCpuProcessorId:true}")
  private boolean hcCpuProcessorId;

  // Return the CPU temperature information in health messages.
  @Value("${hcCpuTemperature:true}")
  private boolean hcCpuTemperature;

  // Return the available memory information in health messages.
  @Value("${hcMemoryAvailable:true}")
  private boolean hcMemoryAvailable;

  // Return the total memory information in health messages.
  @Value("${hcMemoryTotal:true}")
  private boolean hcMemoryTotal;

  // Return the load in the last 1' information in health messages.
  @Value("${hcLoad1:true}")
  private boolean hcLoad1;

  // Return the load in the last 5' information in health messages.
  @Value("${hcLoad5:true}")
  private boolean hcLoad5;

  // Return the load in the last 15' information in health messages.
  @Value("${hcLoad15:true}")
  private boolean hcLoad15;

  // Return information about filesystems usage in health messages.
  @Value("${hcFs:true}")
  private boolean hcFs;

  // A comma-separated list of filestystems to include in health messages. If left empty, all
  // discovered filesystems will be included.
  @Value("${hcFilterFs:}")
  private String hcFilterFs;

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

  // A comma-separated list of the commands this device supports.
  @Value("${supportedCommands:PROVISIONING_CHECK_NEW,PING,HEALTH,REBOOT,EXECUTE}")
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
}
