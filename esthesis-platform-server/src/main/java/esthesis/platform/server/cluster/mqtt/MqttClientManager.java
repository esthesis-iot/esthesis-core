package esthesis.platform.server.cluster.mqtt;

import esthesis.platform.server.cluster.ClusterInfoService;
import esthesis.platform.server.cluster.zookeeper.ZookeeperClientManager;
import esthesis.platform.server.config.AppConstants.MqttTopics;
import esthesis.platform.server.config.AppConstants.Zookeeper;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.events.ClusterEvent;
import esthesis.platform.server.events.ClusterEvent.CLUSTER_EVENT_TYPE;
import esthesis.platform.server.events.LocalEvent;
import esthesis.platform.server.mapper.MQTTMessageMapper;
import esthesis.platform.server.mapper.MQTTServerMapper;
import esthesis.platform.server.repository.MQTTServerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Validated
@Transactional
public class MqttClientManager {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(MqttClientManager.class.getName());
  // The list of mqtt clients connected to.
  private Map<Long, ManagedMqttClient> mqttClients = new HashMap<>();
  private final static String MQTT_PROTOCOL_PREFIX = "tcp://";
  private final AppProperties appProperties;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final MQTTMessageMapper mqttMessageMapper;
  private final ClusterInfoService clusterInfoService;
  private final ZookeeperClientManager zookeeperClientManager;
  private final MQTTServerRepository mqttServerRepository;
  private final MQTTServerMapper mqttServerMapper;

  public MqttClientManager(AppProperties appProperties,
    ApplicationEventPublisher applicationEventPublisher,
    MQTTMessageMapper mqttMessageMapper,
    ClusterInfoService clusterInfoService,
    ZookeeperClientManager zookeeperClientManager,
    MQTTServerRepository mqttServerRepository,
    MQTTServerMapper mqttServerMapper) {
    this.appProperties = appProperties;
    this.applicationEventPublisher = applicationEventPublisher;
    this.mqttMessageMapper = mqttMessageMapper;
    this.clusterInfoService = clusterInfoService;
    this.zookeeperClientManager = zookeeperClientManager;
    this.mqttServerRepository = mqttServerRepository;
    this.mqttServerMapper = mqttServerMapper;
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
    mqttServerRepository.findAllByState(true).forEach(mqttServer -> {
      try {
        disconnect(mqttServer.getId());
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Could not disconnect from all MQTT servers.", e);
      }
    });
  }

  private void connect(MQTTServerDTO mqttServerDTO) {
    mqttServerDTO.setIpAddress(MQTT_PROTOCOL_PREFIX + mqttServerDTO.getIpAddress());
    try {
      // Create a new client to connect to the MQTT server.
      LOGGER.log(Level.FINE, "Connecting to MQTT server {0}.", mqttServerDTO.getIpAddress());
      ManagedMqttClient client = new ManagedMqttClient(
        mqttServerDTO.getIpAddress(),
        appProperties.getNodeId(),
        mqttServerDTO.getTopicTelemetry(),
        mqttServerDTO.getTopicMetadata(),
        applicationEventPublisher,
        mqttMessageMapper,
        mqttServerDTO.getTopicControl() + MqttTopics.REQUEST,
        mqttServerDTO.getTopicControl() + MqttTopics.REPLY
      );

      // Connect to the MQTT server.
      client.connect();

      // Participate in leader election if in cluster.
      if (clusterInfoService.isStandalone()) {
        client.subscribe();
      } else {
        client.participateInLeaderElection(mqttServerDTO.getId(),
          zookeeperClientManager.getZookeeperClient());
      }

      mqttClients.put(mqttServerDTO.getId(), client);
    } catch (Exception e) {
      LOGGER
        .log(Level.SEVERE, MessageFormat.format("Could not connect to MQTT server {0}.",
          mqttServerDTO.getIpAddress()), e);
    }
  }

  @EventListener
  public void onApplicationEventTx(LocalEvent event) {
    switch (event.getEventType()) {
      case CONNECTIVITY_ZOOKEEPER_CONNECTED:
        LOGGER.log(Level.FINEST, "EVENT: {0}.", event);
        connectAll();
        break;
      case CONNECTIVITY_ZOOKEEPER_DISCONNECTED:
        LOGGER.log(Level.FINEST, "EVENT: {0}.", event);
        disconnectAll();
        break;
      case CONFIGURATION_MQTT:
        LOGGER.log(Level.FINEST, "EVENT: {0}.", event);
        disconnectAll();
        connectAll();
        if (!clusterInfoService.isStandalone() && !event.isClusterEvent()) {
          try {
            zookeeperClientManager.getZookeeperClient().setData()
              .forPath(Zookeeper.MQTT_CONFIGURATION_EVENT_PATH,
                new ClusterEvent(CLUSTER_EVENT_TYPE.CONFIGURATION_MQTT)
                  .setEmitterNode(appProperties.getNodeId()).toByteArray());
          } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not process PlatformEvent.", e);
          }
        }
        break;
    }
  }
}
