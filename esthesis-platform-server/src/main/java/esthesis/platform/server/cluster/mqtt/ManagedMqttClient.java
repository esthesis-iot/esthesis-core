package esthesis.platform.server.cluster.mqtt;

import static esthesis.platform.server.config.AppConstants.Zookeeper.LEADER_ELECTION_PATH_MQTT;

import esthesis.extension.common.config.AppConstants.Mqtt;
import esthesis.platform.server.cluster.zookeeper.ZookeeperClientManager;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.events.PingEvent;
import esthesis.platform.server.mapper.MQTTMessageMapper;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;

@Log
public class ManagedMqttClient {

  private MQTTServerDTO mqttServerDTO;
  private String clientName;
  private IMqttClient client;
  private ApplicationEventPublisher applicationEventPublisher;
  private MQTTMessageMapper mqttMessageMapper;
  private LeaderLatch leaderLatch;
  private boolean isStandalone;
  private final ZookeeperClientManager zookeeperClientManager;

  public ManagedMqttClient(MQTTServerDTO mqttServerDTO,
    final String clientName,
    final ApplicationEventPublisher applicationEventPublisher,
    final MQTTMessageMapper mqttMessageMapper,
    final boolean isStandalone,
    final ZookeeperClientManager zookeeperClientManager) {
    this.mqttServerDTO = mqttServerDTO;
    this.clientName = clientName;
    this.applicationEventPublisher = applicationEventPublisher;
    this.mqttMessageMapper = mqttMessageMapper;
    this.zookeeperClientManager = zookeeperClientManager;
    this.isStandalone = isStandalone;
  }

  protected void connect() throws MqttException {
    // Connect to the MQTT server.
    client = new MqttClient(mqttServerDTO.getIpAddress(), clientName, new MemoryPersistence());
    MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(true);
    client.setCallback(new MqttCallbackExtended() {
      @Override
      public void connectionLost(Throwable cause) {
        log.log(Level.FINEST,
          MessageFormat.format("Lost connection to MQTT server {0}.", mqttServerDTO.getIpAddress()),
          cause);
      }

      @Override
      public void messageArrived(String topic, MqttMessage message) {
      }

      @Override
      public void deliveryComplete(IMqttDeliveryToken token) {
      }

      @Override
      public void connectComplete(boolean reconnect, String serverURI) {
        // Participate in leader election if in cluster.
        if (isStandalone) {
          try {
            unsubscribe();
            subscribe();
          } catch (MqttException e) {
            log.log(Level.SEVERE,
              MessageFormat.format("Could not subscribe to MQTT server {0}.", serverURI), e);
          }
        } else {
          try {
            unsubscribe();
            participateInLeaderElection(mqttServerDTO.getId(),
              zookeeperClientManager.getZookeeperClient());
          } catch (Exception e) {
            log.log(Level.SEVERE,
              MessageFormat
                .format("Could not participate in leader election for MQTT server {0}.", serverURI),
              e);
          }
        }
      }
    });
    client.connect(options);

    log.log(Level.FINE, "Connected to MQTT server {0}.", mqttServerDTO.getIpAddress());
  }

  protected void disconnect() throws IOException {
    log.log(Level.FINE, "Disconnecting from MQTT server {0}.", mqttServerDTO.getIpAddress());

    // Remove the client from leader election.
    if (leaderLatch != null) {
      leaderLatch.close();
    }

    // Disconnect the client.
    try {
      unsubscribe();
      client.disconnect();
      client.close();
    } catch (MqttException e) {
      try {
        client.disconnectForcibly();
        client.close();
      } catch (MqttException e1) {
        log.log(Level.SEVERE, MessageFormat.format("Could not disconnect from MQTT server {0}.",
          mqttServerDTO.getIpAddress()), e);
      }
    }

    log.log(Level.FINE, "Disconnected from MQTT server {0}.", mqttServerDTO.getIpAddress());
  }

  /**
   * Subscribe to MQTT topics.
   */
  protected void subscribe() throws MqttException {
    log.log(Level.FINE, "Subscribing to {0} on MQTT server {1}.",
      new Object[]{StringUtils.joinWith(", ", Mqtt.EventType.TELEMETRY,
        Mqtt.EventType.METADATA, Mqtt.EventType.CONTROL_REPLY),
        mqttServerDTO.getIpAddress()});

    client.subscribe("/" + Mqtt.EventType.TELEMETRY + "/#", (topic, message) ->
      applicationEventPublisher.publishEvent(
        mqttMessageMapper.map(message)
          .setTopic(topic)
          .setHardwareId(topic.substring(topic.lastIndexOf('/') + 1))
          .setId(UUID.randomUUID().toString())));

    client.subscribe("/" + Mqtt.EventType.METADATA + "/#", (topic, message) ->
      applicationEventPublisher.publishEvent(
        mqttMessageMapper.map(message)
          .setTopic(topic)
          .setHardwareId(topic.substring(topic.lastIndexOf('/') + 1))
          .setId(UUID.randomUUID().toString())));

    client.subscribe("/" + Mqtt.EventType.CONTROL_REPLY + "/#", (topic, message) ->
      applicationEventPublisher.publishEvent(
        mqttMessageMapper.map(message)
          .setTopic(topic)
          .setHardwareId(topic.substring(topic.lastIndexOf('/') + 1))
          .setId(UUID.randomUUID().toString())));

    client.subscribe("/" + Mqtt.EventType.PING + "/#", (topic, message) ->
      applicationEventPublisher
        .publishEvent(new PingEvent(topic.substring(topic.lastIndexOf('/') + 1))));
  }

  protected void unsubscribe() throws MqttException {
    if (client != null && client.isConnected()) {
      client.unsubscribe(
        new String[]{"/" + Mqtt.EventType.TELEMETRY + "/#",
          "/" + Mqtt.EventType.METADATA + "/#",
          "/" + Mqtt.EventType.CONTROL_REPLY + "/#"});
    }
  }

  protected void participateInLeaderElection(long mqttServerId, CuratorFramework curatorFramework)
  throws Exception {
    curatorFramework.checkExists().creatingParentsIfNeeded()
      .forPath(LEADER_ELECTION_PATH_MQTT + "/" + mqttServerId);

    // Participate in leader election.
    log.log(Level.FINE, "Requesting participation in MQTT leader election for {0}.",
      mqttServerDTO.getIpAddress());
    leaderLatch = new LeaderLatch(curatorFramework,
      LEADER_ELECTION_PATH_MQTT + "/" + mqttServerId, clientName);

    // Add listeners for leadership status change.
    leaderLatch.addListener(new LeaderLatchListener() {
      @Override
      public void isLeader() {
        log.log(Level.FINEST, "Became MQTT leader for {0}.", mqttServerDTO.getIpAddress());
        try {
          subscribe();
        } catch (MqttException e) {
          log.log(Level.SEVERE, MessageFormat.format("Could not subscribe to MQTT topics on server "
            + "{0}.", mqttServerDTO.getIpAddress()), e);
        }
      }

      @Override
      public void notLeader() {
        log.log(Level.FINEST, "Lost MQTT leadership for {0}.", mqttServerDTO.getIpAddress());
        try {
          unsubscribe();
        } catch (MqttException e) {
          log.log(Level.SEVERE, MessageFormat.format("Could not unsubscribe from MQTT topics on "
            + "server {0}.", mqttServerDTO.getIpAddress()), e);
        }
      }
    });

    leaderLatch.start();
    log.log(Level.FINE, "Established participation in MQTT leader election for {0}.",
      mqttServerDTO.getIpAddress());
  }
}
