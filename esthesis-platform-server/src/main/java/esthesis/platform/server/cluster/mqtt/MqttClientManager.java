package esthesis.platform.server.cluster.mqtt;

import com.eurodyn.qlack.fuse.crypto.CryptoAsymmetricService;
import com.eurodyn.qlack.fuse.crypto.CryptoCAService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.extension.common.config.AppConstants.Mqtt.EventType;
import esthesis.extension.device.config.AppConstants.MqttCommand;
import esthesis.extension.device.control.MqttControlCommand;
import esthesis.platform.server.cluster.ClusterInfoService;
import esthesis.platform.server.cluster.zookeeper.ZookeeperClientManager;
import esthesis.platform.server.config.AppConstants.Zookeeper;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.events.ClusterEvent;
import esthesis.platform.server.events.ClusterEvent.CLUSTER_EVENT_TYPE;
import esthesis.platform.server.events.LocalEvent;
import esthesis.platform.server.mapper.MQTTMessageMapper;
import esthesis.platform.server.mapper.MQTTServerMapper;
import esthesis.platform.server.repository.MQTTServerRepository;
import esthesis.platform.server.service.MQTTService;
import esthesis.platform.server.service.SecurityService;
import lombok.extern.java.Log;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

@Log
@Component
@Validated
@Transactional
public class MqttClientManager {

  // The list of mqtt clients connected to.
  private Map<Long, ManagedMqttClient> mqttClients = new HashMap<>();
  private final AppProperties appProperties;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final MQTTMessageMapper mqttMessageMapper;
  private final ClusterInfoService clusterInfoService;
  private final ZookeeperClientManager zookeeperClientManager;
  private final MQTTServerRepository mqttServerRepository;
  private final MQTTServerMapper mqttServerMapper;
  private final MQTTService mqttService;
  private final ObjectMapper objectMapper;
  private final CryptoCAService cryptoCAService;
  private final CryptoAsymmetricService cryptoAsymmetricService;
  private final SecurityService securityService;

  public MqttClientManager(AppProperties appProperties,
    ApplicationEventPublisher applicationEventPublisher,
    MQTTMessageMapper mqttMessageMapper,
    ClusterInfoService clusterInfoService,
    ZookeeperClientManager zookeeperClientManager,
    MQTTServerRepository mqttServerRepository,
    MQTTServerMapper mqttServerMapper, MQTTService mqttService,
    ObjectMapper objectMapper, CryptoCAService cryptoCAService,
    CryptoAsymmetricService cryptoAsymmetricService,
    SecurityService securityService) {
    this.appProperties = appProperties;
    this.applicationEventPublisher = applicationEventPublisher;
    this.mqttMessageMapper = mqttMessageMapper;
    this.clusterInfoService = clusterInfoService;
    this.zookeeperClientManager = zookeeperClientManager;
    this.mqttServerRepository = mqttServerRepository;
    this.mqttServerMapper = mqttServerMapper;
    this.mqttService = mqttService;
    this.objectMapper = objectMapper;
    this.cryptoCAService = cryptoCAService;
    this.cryptoAsymmetricService = cryptoAsymmetricService;
    this.securityService = securityService;
  }

  private void disconnect(long mqttServerId) throws IOException {
    if (mqttClients.get(mqttServerId) != null) {
      mqttClients.get(mqttServerId).disconnect();
      mqttClients.remove(mqttServerId);
    }
  }

  private void connectAll() {
    mqttServerRepository.findAllByState(true).forEach(mqttServer ->
      connect(mqttServerMapper.map(mqttServer)));
  }

  private void disconnectAll() {
    mqttServerRepository.findAll().forEach(mqttServer -> {
      try {
        disconnect(mqttServer.getId());
      } catch (IOException e) {
        log.log(Level.SEVERE, "Could not disconnect from all MQTT servers.", e);
      }
    });
  }

  private void connect(MQTTServerDTO mqttServerDTO) {
    mqttServerDTO.setIpAddress(mqttServerDTO.getIpAddress());
    try {
      // Create a new client to connect to the MQTT server.
      log.log(Level.FINE, "Connecting to MQTT server {0}.", mqttServerDTO.getIpAddress());
      ManagedMqttClient client = new ManagedMqttClient(
        mqttServerDTO, appProperties.getNodeId(), applicationEventPublisher, mqttMessageMapper,
        clusterInfoService.isStandalone(), zookeeperClientManager,
        cryptoCAService, cryptoAsymmetricService, appProperties, securityService);

      // Connect to the MQTT server.
      client.connect();

      mqttClients.put(mqttServerDTO.getId(), client);
    } catch (Exception e) {
      log.log(Level.SEVERE,
        MessageFormat.format("Could not connect to MQTT server {0}.", mqttServerDTO.getIpAddress()),
        e);
    }
  }

  @EventListener
  public void onApplicationEventTx(LocalEvent event) {
    log.log(Level.FINEST, "Received event {0}.", event);
    switch (event.getEventType()) {
      case CONNECTIVITY_ZOOKEEPER_CONNECTED:
        log.log(Level.FINEST, "Handling event: {0}.", event);
        connectAll();
        break;
      case CONNECTIVITY_ZOOKEEPER_DISCONNECTED:
        log.log(Level.FINEST, "Handling event: {0}.", event);
        disconnectAll();
        break;
      case CONFIGURATION_MQTT:
        log.log(Level.FINEST, "Handling event: {0}.", event);
        disconnectAll();
        connectAll();
        if (!clusterInfoService.isStandalone() && !event.isClusterEvent()) {
          try {
            zookeeperClientManager.getZookeeperClient().setData()
              .forPath(Zookeeper.MQTT_CONFIGURATION_EVENT_PATH,
                new ClusterEvent(CLUSTER_EVENT_TYPE.CONFIGURATION_MQTT)
                  .setEmitterNode(appProperties.getNodeId()).toByteArray());
          } catch (Exception e) {
            log.log(Level.SEVERE, "Could not process LocalEvent.", e);
          }
        }
        break;
    }
  }

  public void sendCommand(String hardwareId, List<Long> tags, MqttCommand command,
    String commandArgumens) {
    Optional<MQTTServerDTO> mqttServerDTO = mqttService.matchByTag(tags);
    if (mqttServerDTO.isPresent()) {
      MqttControlCommand mqttControlCommand = new MqttControlCommand()
        .setId(UUID.randomUUID().toString())
        .setCommand(command)
        .setCommandPayload(commandArgumens.getBytes(StandardCharsets.UTF_8))
        .setSentOn(Instant.now());
      final ManagedMqttClient managedMqttClient = mqttClients.get(mqttServerDTO.get().getId());
      final String topic = "/" + EventType.CONTROL_REQUEST + "/" + hardwareId;
      log.log(Level.FINEST, "Publishing command {0} to MQTT server {1}.", new Object[]{command,
        managedMqttClient.getUri() + topic});
      try {
        managedMqttClient
          .publish(topic, objectMapper.writeValueAsBytes(mqttControlCommand), 0, false);
      } catch (JsonProcessingException e) {
        log.log(Level.SEVERE,
          MessageFormat.format("Could not send MQTT command {0}.", command.toString()), e);
      }
    } else {
      log
        .log(Level.WARNING, "Could not find MQTT server for tags {0} to send device command {1}.",
          new Object[]{tags, command});
    }
  }
}
