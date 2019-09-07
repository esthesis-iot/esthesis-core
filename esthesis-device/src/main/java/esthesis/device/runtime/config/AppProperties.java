package esthesis.device.runtime.config;

import esthesis.device.runtime.config.AppConstants.ExitCode;
import esthesis.device.runtime.resolver.id.HardwareIdResolverUtil;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

  // The root folder under which secure persistent storage is provided. If this is left empty, the storageRoot is used.
  @Value("${secureStorageRoot:${storageRoot}}")
  private String secureStorageRoot;

  // The root folder to store remotely retrieved provisioning packages.
  @Value("${provisioningRoot:${storageRoot}/provisioning}")
  private String provisioningRoot;

  // The root folder to store remotely retrieved provisioning packages.
  @Value("${provisioningTempRoot:${storageRoot}/provisioning/.tmp}")
  private String provisioningTempRoot;

  // The maximum number a request is retried.
  @Value("${device.runtime.request.attempts:100}")
  private int requestAttempts;

  // Number of milliseconds to wait before trying again.
  @Value("${device.runtime.request.backoff:1000}")
  private int requestRetryBackoff;

  // The maximum number of minutes to wait between attempts.
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

  // Whether provisioning packages should be encrypted.
  @Value("${provisioningEncrypted:false}")
  private boolean provisioningEncrypted;

  // Whether provisioning packages should be signed.
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

  // How to fork the external script during provisioning.
  // soft: The script is called as a child process, controlled by the runtime agent. As soon as the
  //       agent terminates, the provisioning script terminates too.
  // hard: The script is called as an independent process, not controlled by the runtime agent.
  @Value("${provisioningForkType:soft}")
  private String provisioningForkType;

  // A flag indicating that initial device registration is skipped.
  @Value("${skipRegistration:false}")
  private boolean skipRegistration;

  // A flag indicating that startup is paused until a key is pressed.
  @Value("${pauseStartup:false}")
  private boolean pauseStartup;

  // A script to be called after every time a provisioning package is downloaded.
  // The script is handed the following parameters:
  // 1. The full pathname to the provisioning package.
  // 2. Whether this is an initial provisioning or not (as true/false values).
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

  // How often health the platform is pinged.
  // See also HealthMetadataCollection#ping.
  @Value("${pingFreqMsec:60000}")
  private long pingFreqMsec;

  // How long to wait before starting pinging the platform.
  // See also HealthMetadataCollection#ping.
  @Value("${pingInitialDelayMsec:60000}")
  private long pingInitialDelayMsec;

  @Value("${hcOsManufacturer:true}")
  private boolean hcOsManufacturer;

  @Value("${hcOsVersion:true}")
  private boolean hcOsVersion;

  @Value("${hcHwSerial:true}")
  private boolean hcHwSerial;

  @Value("${hcCpuPhysicalPackage:true}")
  private boolean hcCpuPhysicalPackage;

  @Value("${hcCpuPhysicalCores:true}")
  private boolean hcCpuPhysicalCores;

  @Value("${hcCpuLogicalCores:true}")
  private boolean hcCpuLogicalCores;

  @Value("${hcCpuIdentifier:true}")
  private boolean hcCpuIdentifier;

  @Value("${hcCpuProcessorId:true}")
  private boolean hcCpuProcessorId;

  @Value("${hcCpuTemperature:true}")
  private boolean hcCpuTemperature;

  @Value("${hcMemoryAvailable:true}")
  private boolean hcMemoryAvailable;

  @Value("${hcMemoryTotal:true}")
  private boolean hcMemoryTotal;

  @Value("${hcLoad1:true}")
  private boolean hcLoad1;

  @Value("${hcLoad5:true}")
  private boolean hcLoad5;

  @Value("${hcLoad15:true}")
  private boolean hcLoad15;

  @Value("${hcFs:true}")
  private boolean hcFs;

  @Value("${hcCurrentTime:true}")
  private boolean hcCurrentTime;

  @Value("${hcUpTime:true}")
  private boolean hcUpTime;

  @Value("${hcIpAddress:true}")
  private boolean hcIpAddress;

  @Value("${runtimeVersion:true}")
  private boolean runtimeVersion;

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

  public String getHardwareId() {
    try {
      return HardwareIdResolverUtil.resolve(hardwareId);
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | IOException e) {
      log.log(Level.SEVERE, "Could not obtain device ID.", e);
      System.exit(ExitCode.CANNOT_FIND_DEVICE_ID);
      return null;
    }
  }
}
