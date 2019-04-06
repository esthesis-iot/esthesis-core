package esthesis.platform.server.service;

import static esthesis.platform.server.config.AppConstants.Zookeeper.LEADER_ELECTION_PATH_MQTT;

import esthesis.platform.server.config.AppConstants.Event;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.events.MQTTConfigurationChangedEvent;
import esthesis.platform.server.events.ZookeeperConnectivityEvent;
import esthesis.platform.server.events.ZookeeperConnectivityEvent.EVENT_TYPE;
import esthesis.platform.server.mapper.MQTTMessageMapper;
import esthesis.platform.server.mapper.MQTTServerMapper;
import esthesis.platform.server.model.MqttServer;
import esthesis.platform.server.repository.MQTTServerRepository;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Transactional
@Validated
public class MQTTService extends BaseService<MQTTServerDTO, MqttServer> implements
    ApplicationListener<ZookeeperConnectivityEvent> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(MQTTService.class.getName());
  private final static String MQTT_PROTOCOL_PREFIX = "tcp://";

  // Individual leader latches for all MQTT servers.
  private Map<Long, LeaderLatch> leaderLatchMQTT = new HashMap<>();

  // A list of active mqtt clients.
  private Map<Long, IMqttClient> mqttClients = new HashMap<>();

  private final MQTTServerMapper mqttServerMapper;
  private final MQTTServerRepository mqttServerRepository;
  private final AppProperties appProperties;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ZookeeperService zookeeperService;
  private final MQTTMessageMapper mqttMessageMapper;

  public MQTTService(MQTTServerMapper mqttServerMapper, MQTTServerRepository mqttServerRepository,
      AppProperties appProperties, ApplicationEventPublisher applicationEventPublisher,
      ZookeeperService zookeeperService, MQTTMessageMapper mqttMessageMapper) {
    this.mqttServerMapper = mqttServerMapper;
    this.mqttServerRepository = mqttServerRepository;
    this.appProperties = appProperties;
    this.applicationEventPublisher = applicationEventPublisher;
    this.zookeeperService = zookeeperService;
    this.mqttMessageMapper = mqttMessageMapper;
  }

  private void participateInLeaderElection(MQTTServerDTO mqttServerDTO) throws Exception {
    final CuratorFramework zookeeperClient = zookeeperService.getZookeeperClient();
    zookeeperClient.checkExists().creatingParentsIfNeeded()
        .forPath(LEADER_ELECTION_PATH_MQTT + "/" + mqttServerDTO.getId());

    // Participate in leader election.
    LOGGER.log(Level.FINE, "Requesting participation in MQTT leader election for {0}.", mqttServerDTO.getIpAddress());
    LeaderLatch leaderLatch = new LeaderLatch(zookeeperClient,
        LEADER_ELECTION_PATH_MQTT + "/" + mqttServerDTO.getId(), appProperties.getNodeId());

    // Add listeners for leadership status change.
    leaderLatch.addListener(new LeaderLatchListener() {
      @Override
      public void isLeader() {
        LOGGER.log(Level.FINEST, "Became MQTT leader for {0}.", mqttServerDTO.getIpAddress());
        try {
          subscribe(mqttClients.get(mqttServerDTO.getId()), mqttServerDTO);
        } catch (MqttException e) {
          LOGGER.log(Level.SEVERE, "Could not subscribe to MQTT topics on server {0}.", mqttServerDTO.getIpAddress());
        }
      }

      @Override
      public void notLeader() {
        LOGGER.log(Level.FINEST, "Lost MQTT leadership for {0}.", mqttServerDTO.getIpAddress());
        final IMqttClient mqttClient = mqttClients.get(mqttServerDTO.getId());
        if (mqttClient != null) {
          try {
            unsubscribe(mqttClient, mqttServerDTO);
          } catch (MqttException e) {
            LOGGER.log(Level.SEVERE, "Could not unsubscribe from MQTT topics on server {0}.",
                mqttServerDTO.getIpAddress());
          }
        }
      }
    });

    leaderLatch.start();
    leaderLatchMQTT.put(mqttServerDTO.getId(), leaderLatch);
    LOGGER.log(Level.FINE,
        "Established participation in MQTT leader election for {0}.", mqttServerDTO.getIpAddress());
  }

  /**
   * Subscribe to MQTT topics.
   */
  private void subscribe(IMqttClient mqttClient, MQTTServerDTO mqttServerDTO) throws MqttException {
    LOGGER.log(Level.FINE, "Subscribing to {0} on MQTT server {1}.", new Object[]{
        mqttServerDTO.getTopicTelemetry() + ", " + mqttServerDTO.getTopicMetadata(),
        mqttServerDTO.getIpAddress()});

    mqttClient.subscribe(mqttServerDTO.getTopicTelemetry() + "/#",
        (topic, message) -> applicationEventPublisher.publishEvent(
            mqttMessageMapper.mapToTelemetryEvent(message)
                .setTopic(topic)
                .setDeviceId(topic.substring(topic.lastIndexOf('/')+ 1))
                .setId(UUID.randomUUID().toString())));
    mqttClient.subscribe(mqttServerDTO.getTopicMetadata() + "/#",
        (topic, message) -> applicationEventPublisher.publishEvent(
            mqttMessageMapper.mapToMetadataEvent(message)
                .setTopic(topic)
                .setDeviceId(topic.substring(topic.lastIndexOf('/')+ 1))
                .setId(UUID.randomUUID().toString())));
  }

  private void unsubscribe(IMqttClient mqttClient, MQTTServerDTO mqttServerDTO) throws MqttException {
    mqttClient.unsubscribe(mqttServerDTO.getTopicTelemetry());
    mqttClient.unsubscribe(mqttServerDTO.getTopicMetadata());
  }

  private void connect(MQTTServerDTO mqttServerDTO) {
    mqttServerDTO.setIpAddress(MQTT_PROTOCOL_PREFIX + mqttServerDTO.getIpAddress());
    try {
      // Create a new client to connect to the MQTT server.
      LOGGER.log(Level.FINE, "Connecting to MQTT server {0}.", mqttServerDTO.getIpAddress());
      IMqttClient client = new MqttClient(mqttServerDTO.getIpAddress(), appProperties.getNodeId(),
          new MemoryPersistence());
      MqttConnectOptions options = new MqttConnectOptions();
      options.setAutomaticReconnect(true);
      options.setCleanSession(true);
      client.connect(options);
      mqttClients.put(mqttServerDTO.getId(), client);
      LOGGER.log(Level.FINE, "Connected to MQTT server {0}.", mqttServerDTO.getIpAddress());

      // Participate in leader election if in cluster.
      if (zookeeperService.isCluster()) {
        participateInLeaderElection(mqttServerDTO);
      } else {
        subscribe(client, mqttServerDTO);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Could not connect to MQTT server {0}.", mqttServerDTO.getIpAddress());
    }
  }

  public void disconnect(long mqttServerId, boolean closeLeaderLatch) throws IOException {
    if (mqttClients.get(mqttServerId) != null || leaderLatchMQTT.get(mqttServerId) != null) {
      final MQTTServerDTO mqttServerDTO = findById(mqttServerId);
      LOGGER.log(Level.FINE, "Disconnecting from MQTT server {0}.", mqttServerDTO.getIpAddress());

      // Remove the client from leader election.
      if (closeLeaderLatch && leaderLatchMQTT.get(mqttServerId) != null) {
        leaderLatchMQTT.get(mqttServerId).close();
        leaderLatchMQTT.remove(mqttServerId);
      }

      // Disconnect the client.
      IMqttClient mqttClient = mqttClients.get(mqttServerId);
      if (mqttClient != null) {
        try {
          unsubscribe(mqttClient, mqttServerDTO);
          mqttClient.disconnect();
        } catch (MqttException e) {
          try {
            mqttClient.disconnectForcibly();
          } catch (MqttException e1) {
            LOGGER.log(Level.SEVERE, "Could not disconnect from MQTT server {0}.", mqttClient.getServerURI());
          }
        }
        mqttClients.remove(mqttServerId);
      }

      LOGGER.log(Level.FINE, "Disconnected from MQTT server {0}.", mqttServerDTO.getIpAddress());
    }
  }

  private void publishMqttServerConfigurationChange(MQTTConfigurationChangedEvent mqttConfigurationChangedEvent)
      throws Exception {
    zookeeperService.getZookeeperClient().setData()
        .forPath(Event.MQTT_CONFIGURATION_EVENT_PATH, mqttConfigurationChangedEvent.toByteArray());
  }

  public void connect(long mqttServerId) {
    connect(findById(mqttServerId));
  }

  public List<MQTTServerDTO> findActive() {
    return mqttServerMapper.map(mqttServerRepository.findAllByState(true));
  }

  @Override
  public MQTTServerDTO save(MQTTServerDTO dto) {
    // Find the previous state of this MQTT server if existed.
    MQTTServerDTO mqttServerDTOBefore = null;
    if (dto.getId() != 0) {
      mqttServerDTOBefore = findById(dto.getId());
    }

    // Save the MQTT server.
    dto = super.save(dto);

    // Restart the MQTT client if necessary.
    try {
      if (mqttServerDTOBefore != null && mqttServerDTOBefore.getState()) {
        disconnect(dto.getId(), true);
      }

      if (dto.getState()) {
        connect(dto);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not reconnect to MQTT server.", e);
    }

    // Inform cluster that MQTT server configuration changed.
    if (zookeeperService.isCluster()) {
      LOGGER.log(Level.FINEST, "Publishing MQTT configuration change event.");
      try {
        publishMqttServerConfigurationChange(new MQTTConfigurationChangedEvent()
            .setMqttServerId(dto.getId())
            .setNodeInitiatingChange(appProperties.getNodeId())
            .setStateAfter(dto.getState())
            .setDeleted(false)
            .setStateBefore(mqttServerDTOBefore != null ? mqttServerDTOBefore.getState() : false));
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Could not publish MQTT configuration change event.", e);
      }
    }

    return dto;
  }

  @Override
  public MQTTServerDTO deleteById(long id) {
    final MQTTServerDTO mqttServerDTO = super.deleteById(id);
    try {
      disconnect(id, true);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not disconnect from MQTT server.", e);
    }

    // Inform cluster that MQTT server configuration changed.
    if (zookeeperService.isCluster()) {
      LOGGER.log(Level.FINEST, "Publishing MQTT configuration change event.");
      try {
        publishMqttServerConfigurationChange(new MQTTConfigurationChangedEvent()
            .setMqttServerId(mqttServerDTO.getId())
            .setNodeInitiatingChange(appProperties.getNodeId())
            .setDeleted(true)
            .setStateAfter(mqttServerDTO.getState()));
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Could not publish MQTT configuration change event.", e);
      }
    }

    return mqttServerDTO;
  }

  @Override
  public void onApplicationEvent(ZookeeperConnectivityEvent event) {
    synchronized (this) {
      if (event.getEventType() == EVENT_TYPE.CONNECTED) {
        try {
          for (MQTTServerDTO mqttServerDTO : findActive()) {
            connect(mqttServerDTO);
          }
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Could not participate in leader election.", e);
        }
      } else if (event.getEventType() == EVENT_TYPE.DISCONNECTED) {
        try {
          for (MQTTServerDTO mqttServerDTO : findActive()) {
            // No need to close the leader latch as it was created with a Zookeeper client which is now disconnected
            // anyway (it is closed automatically). Only remove it from the list of latches handled.
            disconnect(mqttServerDTO.getId(), false);
          }
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Could not participate in leader election.", e);
        }
      }
    }
  }

  public boolean isLeader(long mqttServerId) {
    if (leaderLatchMQTT.get(mqttServerId) != null) {
      return leaderLatchMQTT.get(mqttServerId).hasLeadership();
    } else if (!zookeeperService.isCluster()) {
      return true;
    } else {
      LOGGER.log(Level.WARNING, "Requested leader status for non-existing MQTT leader latch for MQTT server id {0}.",
          mqttServerId);
      return false;
    }
  }

  public void releaseLeadership(long mqttServerId) throws Exception {
    final MQTTServerDTO mqttServerDTO = findById(mqttServerId);

    // Release leadership.
    LOGGER.log(Level.FINE, "Releasing leadership for MQTT server {0}.", mqttServerDTO.getIpAddress());
    disconnect(mqttServerId, true);
    connect(mqttServerDTO);
  }
}
