package esthesis.device.runtime.mqtt;

import com.eurodyn.qlack.common.exception.QMismatchException;
import com.eurodyn.qlack.fuse.crypto.dto.SSLSocketFactoryCertificateDTO;
import com.eurodyn.qlack.fuse.crypto.dto.SSLSocketFactoryDTO;
import com.eurodyn.qlack.fuse.crypto.dto.SSLSocketFactoryPrivateKeyDTO;
import com.eurodyn.qlack.fuse.crypto.service.CryptoSSLService;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.device.runtime.config.AppConstants.Mqtt;
import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.util.SecurityUtil;
import esthesis.platform.backend.common.device.commands.CommandRequestDTO;
import javax.annotation.PreDestroy;
import lombok.extern.java.Log;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;

@Log
@Component
public class MqttClient {

  // JUL reference.
  private final AppProperties appProperties;
  private IMqttClient client;
  private String mqttServerAddress;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ObjectMapper objectMapper;
  private final SecurityUtil securityUtil;
  private final CryptoSSLService cryptoSSLService;

  public MqttClient(AppProperties appProperties,
    ApplicationEventPublisher applicationEventPublisher,
    ObjectMapper objectMapper, SecurityUtil securityUtil,
    CryptoSSLService cryptoSSLService) {
    this.appProperties = appProperties;
    this.applicationEventPublisher = applicationEventPublisher;
    this.objectMapper = objectMapper;
    this.securityUtil = securityUtil;
    this.cryptoSSLService = cryptoSSLService;
  }

  /**
   * Returns the name of the topic suffixed with the device ID.
   *
   * @param mqttEventType The {@link Mqtt.EventType} to suffix.
   */
  private String getTopicForDevice(Mqtt.EventType mqttEventType) {
    switch (mqttEventType) {
      case PING:
        return String.join("/", appProperties.getTopicPing(), appProperties.getHardwareId());
      case METADATA:
        return String.join("/", appProperties.getTopicMetadata(), appProperties.getHardwareId());
      case TELEMETRY:
        return String.join("/", appProperties.getTopicTelemetry(), appProperties.getHardwareId());
      case CONTROL_REPLY:
        return String.join("/", appProperties.getTopicControlReply(), appProperties.getHardwareId());
      case CONTROL_REQUEST:
        return String.join("/", appProperties.getTopicControlRequest(), appProperties.getHardwareId());
      default:
        throw new QMismatchException(
          "Name resolution of an unknown MQTT event type was requested: {0}.", mqttEventType);
    }
  }

  private void subscribe() throws MqttException {
    // Subscribe to receive commands.
    log.log(Level.FINE, "Subscribing to MQTT topic {0}.",
      getTopicForDevice(Mqtt.EventType.CONTROL_REQUEST));
    client.subscribe(getTopicForDevice(Mqtt.EventType.CONTROL_REQUEST), (topic, message) -> {
      try {
        log.log(Level.FINEST, "Received command: {0}", message);
        applicationEventPublisher
          .publishEvent(objectMapper.readValue(message.getPayload(), CommandRequestDTO.class));
      } catch (Exception e) { // Catch any exception to not break client's connection.
        log.log(Level.SEVERE, "Could not parse received command.", e);
      }
    });
  }

  @SuppressWarnings("java:S3776")
  public void connect(String mqttServerAddress) {
    if (client == null || !client.isConnected()) {
      try {
        log.log(Level.FINE, "Connecting to MQTT server {0}.", mqttServerAddress);
        this.mqttServerAddress = mqttServerAddress;
        client = new org.eclipse.paho.client.mqttv3.MqttClient(mqttServerAddress,
          UUID.randomUUID().toString(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setKeepAliveInterval(30);
        client.setCallback(new MqttCallbackExtended() {
          @Override
          public void connectionLost(Throwable cause) {
            // Just report the connection lost, an automatic reconnect will be tried.
            log.log(Level.WARNING, "Lost connection to MQTT server.", cause);
          }

          @Override
          public void messageArrived(String topic, MqttMessage message) {
            // No special handling needed.
          }

          @Override
          public void deliveryComplete(IMqttDeliveryToken token) {
            // No special handling needed.
          }

          @Override
          public void connectComplete(boolean reconnect, String serverURI) {
            log.log(Level.INFO, "Connected to MQTT server {0}.", serverURI);
            if (reconnect) {
              try {
                subscribe();
              } catch (MqttException e) {
                log.log(Level.SEVERE, "Could not subscribe to MQTT topic.", e);
              }
            }
          }
        });

        // If MQTT server is SSL-based, create an SSL context factory.
        if (mqttServerAddress.startsWith("ssl")) {
          if (!securityUtil.isRootCACertificatePresent()) {
            log.log(Level.WARNING,
              "An SSL-based MQTT server is configured, however a root CA has not been pushed to "
                + "the device.");
          }
          options.setSocketFactory(cryptoSSLService.getSocketFactory(SSLSocketFactoryDTO.builder()
            .trustedCertificate(SSLSocketFactoryCertificateDTO.builder().name("ca")
              .pemCertificate(securityUtil.getRootCACertificate()).build())
            .clientCertificate(SSLSocketFactoryCertificateDTO.builder().name("client-cert")
              .pemCertificate(securityUtil.getDeviceCertificate()).build())
            .clientPrivateKey(SSLSocketFactoryPrivateKeyDTO.builder().name("client-private-key")
              .pemPrivateKey(securityUtil.getDevicePrivateKey())
              .algorithm(appProperties.getAsymmetricKeyAlgorithm())
              .build())
            .build()));
        }

        client.connect(options);
        subscribe();
      } catch (MqttException | CertificateException | IOException | KeyStoreException |
        NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException |
        InvalidKeySpecException e) {
        log.log(Level.SEVERE,
          MessageFormat.format("Could not connect to MQTT server {0}.", mqttServerAddress), e);
      }
    }
  }

  @PreDestroy
  public void disconnect() {
    if (client != null && client.isConnected()) {
      log.log(Level.FINE, "Disconnecting from MQTT server {0}.", mqttServerAddress);
      try {
        client.disconnect();
        log.log(Level.FINE, "Disconnected from MQTT server {0}.", mqttServerAddress);
      } catch (MqttException e) {
        log.log(Level.WARNING, "Could not disconnect from MQTT server while "
          + "waiting for inflight messages. Would now try to disconnect forcibly.", e);
        try {
          client.disconnectForcibly();
          log.log(Level.FINE, "Disconnected from MQTT server {0}.",
            mqttServerAddress);
        } catch (MqttException ex) {
          log.log(Level.SEVERE, "Could not disconnect from MQTT server forcibly.",
            ex);
        }
      }
    }
  }

  @Async
  public void publish(Mqtt.EventType mqttEventType, byte[] msg) {
    publish(mqttEventType, msg, 0, false);
  }

  @Async
  public void publish(Mqtt.EventType mqttEventType, byte[] msg, int qos, boolean retain) {
    try {
      String publishingTopic = getTopicForDevice(mqttEventType);
      log.log(Level.FINEST, "Publishing to {0} {1} payload {2}.",
        new Object[]{mqttServerAddress, publishingTopic,
          new String(msg, StandardCharsets.UTF_8)});
      client.publish(publishingTopic, msg, qos, retain);
    } catch (QMismatchException e) {
      log.log(Level.SEVERE, e.getMessage());
    } catch (MqttException e) {
      log.log(Level.SEVERE, "Could not publish message.", e);
    }
  }
}
