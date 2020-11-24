package esthesis.device.runtime;

import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.health.HealthMetadataCollector;
import esthesis.device.runtime.mqtt.MqttClient;
import esthesis.device.runtime.proxy.mqtt.MqttProxyServer;
import esthesis.device.runtime.service.ProvisioningService;
import esthesis.device.runtime.service.RegistrationService;
import esthesis.device.runtime.util.SecurityUtil;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * A bootstrap class to allow component initialization after application has fully started and all
 * Beans are properly configured (in contrast to @{@link javax.annotation.PostConstruct}). This is
 * particularly useful in case a component needs to emit events during initialization (otherwise
 * events are lost since @{@link EventListener} annotations are not fully discovered).
 *
 * In addition, bootstrapping all components in a well-defined sequence here allows greater control
 * over system's boot up times.
 */
@Log
@Component
public class Bootstrap {

  private final AppProperties appProperties;
  private final ProvisioningService provisioningService;
  private final RegistrationService registrationService;
  private final SecurityUtil securityUtil;
  private final RetryTemplate retryTemplate;
  private final MqttProxyServer mqttProxyServer;
  private final MqttClient mqttClient;
  private final HealthMetadataCollector healthMetadataCollector;

  @SuppressWarnings({"java:S3776", "java:S107"})
  public Bootstrap(AppProperties appProperties, ProvisioningService provisioningService,
    RegistrationService registrationService, SecurityUtil securityUtil,
    RetryTemplate retryTemplate, MqttProxyServer mqttProxyServer,
    MqttClient mqttClient, HealthMetadataCollector healthMetadataCollector) {
    this.appProperties = appProperties;
    this.provisioningService = provisioningService;
    this.registrationService = registrationService;
    this.securityUtil = securityUtil;
    this.retryTemplate = retryTemplate;
    this.mqttProxyServer = mqttProxyServer;
    this.mqttClient = mqttClient;
    this.healthMetadataCollector = healthMetadataCollector;
  }

  @EventListener
  @SuppressWarnings({"java:S106", "java:S112", "java:S3776", "java:S4829"})
  public void applicationStarted(ContextRefreshedEvent contextRefreshedEvent) throws Exception {
    if (appProperties.isPauseStartup()) {
      System.out.println("Device booting paused. Press \"ENTER\" to continue...");
      Scanner scanner = new Scanner(System.in);
      scanner.nextLine();
    }

    log.log(Level.INFO, "Initialising esthesis device runtime.");

    // Check if outgoing encryption is required that the public & private keys of the device are
    // available (note that, technically, only the private key is needed).
    if (appProperties.isOutgoingEncrypted() && !securityUtil.areSecurityKeysPresent()) {
      throw new SecurityException("Encryption of outgoing requests is required, however the "
        + "public and private keys of the device are not available.");
    }

    // Create local paths.
    Files.createDirectories(Paths.get(appProperties.getStorageRoot()));
    Files.createDirectories(Paths.get(appProperties.getSecureStorageRoot()));
    Files.createDirectories(Paths.get(appProperties.getProvisioningRoot()));
    Files.createDirectories(Paths.get(appProperties.getProvisioningTempRoot()));

    // Register the device with Platform server.
    if (!appProperties.isSkipRegistration()) {
      retryTemplate.execute(context -> {
        try {
          return registrationService.register();
        } catch (Exception e) {
          log.log(Level.SEVERE, e.getMessage(), e);
          throw e;
        }
      });
    }

    // Perform initial provisioning.
    if (!appProperties.isSkipInitialProvisioning() && !provisioningService
      .isInitialProvisioningDone()) {
      log.log(Level.CONFIG, "Initial provisioning not done. Trying to initialise it now.");
      retryTemplate.execute(context -> {
        try {
          return provisioningService.provisioning();
        } catch (Exception e) {
          log.log(Level.SEVERE, e.getMessage(), e);
          throw e;
        }
      });
    }

    // Start embedded MQTT server.
    if (appProperties.isProxyMqtt()) {
      mqttProxyServer.start();
    }

    // Inform whether the embedded web server is started.
    if (appProperties.isProxyWeb()) {
      log.log(Level.CONFIG, "Embedded Web server started on port {0}.",
        String.valueOf(appProperties.getProxyWebPort()));
    }

    // Start MQTT client, if an MQTT server has been provided.
    if (StringUtils.isNotEmpty(registrationService.getEmbeddedMqttServer())) {
      mqttClient.connect(registrationService.getEmbeddedMqttServer());

      // Publish ping & health data.
      healthMetadataCollector.pingScheduler();
      healthMetadataCollector.collectHealthDataScheduler();
    } else {
      log.log(Level.WARNING, "No MQTT server details were provided for this device.");
    }

    log.log(Level.INFO, "esthesis device runtime initialised.");
  }
}
