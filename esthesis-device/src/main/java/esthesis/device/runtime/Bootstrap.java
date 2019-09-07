package esthesis.device.runtime;

import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.health.HealthMetadataCollector;
import esthesis.device.runtime.mqtt.MqttClient;
import esthesis.device.runtime.proxy.mqtt.MqttProxyServer;
import esthesis.device.runtime.service.ProvisioningService;
import esthesis.device.runtime.service.RegistrationService;
import esthesis.device.runtime.util.SecurityUtil;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A bootstrap class to allow component initialization after application has fully started and all
 * Beans are properly configured (in contrast to @{@link javax.annotation.PostConstruct}). This is
 * particularly useful in case a component needs to emit events during initialization (otherwise
 * events are lost since @{@link EventListener} annotations are not fully discovered).
 *
 * In addition, bootstrapping all components in a well-defined sequence here allows greater control
 * over system's boot up times.
 */
@Component
public class Bootstrap {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(Bootstrap.class.getName());

  private final AppProperties appProperties;
  private final ProvisioningService provisioningService;
  private final RegistrationService registrationService;
  private final SecurityUtil securityUtil;
  private final RetryTemplate retryTemplate;
  private final MqttProxyServer mqttProxyServer;
  private final MqttClient mqttClient;
  private final HealthMetadataCollector healthMetadataCollector;

  public Bootstrap(AppProperties appProperties, ProvisioningService provisioningService,
    RegistrationService registrationService, SecurityUtil securityUtil,
    RetryTemplate retryTemplate, MqttProxyServer mqttProxyServer,
    MqttClient mqttClient,
    HealthMetadataCollector healthMetadataCollector) {
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
  public void applicationStarted(ContextRefreshedEvent contextRefreshedEvent) throws Exception {
    if (appProperties.isPauseStartup()) {
      System.out.println("Device booting paused. Press \"ENTER\" to continue...");
      Scanner scanner = new Scanner(System.in);
      scanner.nextLine();
    }

    LOGGER.log(Level.INFO, "Initialising esthesis device runtime.");

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
          LOGGER.log(Level.SEVERE, e.getMessage(), e);
          throw e;
        }
      });
    }

    // Perform initial provisioning.
    if (!appProperties.isSkipInitialProvisioning() && !provisioningService
      .isInitialProvisioningDone()) {
      LOGGER.log(Level.CONFIG, "Initial provisioning not done. Trying to initialise it now.");
      retryTemplate.execute(context -> {
        try {
          return provisioningService.provisioning();
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, e.getMessage(), e);
          throw e;
        }
      });
    }

    // Start MQTT client.
    mqttClient.connect(registrationService.getEmbeddedMqttServer());

    // Start embedded MQTT server.
    if (appProperties.isProxyMqtt()) {
      mqttProxyServer.start();
    }

    // Inform whether the embedded web server is started.
    if (appProperties.isProxyWeb()) {
      LOGGER.log(Level.CONFIG, "Embedded Web server started on port {0}.",
        String.valueOf(appProperties.getProxyWebPort()));
    }

    // Publish ping & health data as soon as the device is initialised.
    healthMetadataCollector.ping();
    healthMetadataCollector.collectHealthData();

    LOGGER.log(Level.INFO, "esthesis device runtime initialised.");
  }
}
