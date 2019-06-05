package esthesis.platform.server.cluster.mqtt;

import static esthesis.platform.server.config.AppConstants.Zookeeper.LEADER_ELECTION_PATH_MQTT;

import esthesis.extension.datasink.config.AppConstants.Mqtt.EventType;
import esthesis.platform.server.mapper.MQTTMessageMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManagedMqttClient {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ManagedMqttClient.class.getName());

  private String ipAddress;
  private String clientName;
  private String topicTelemetry;
  private String topicMetadata;
  private String topicControlRequest;
  private String topicControlReply;
  private IMqttClient client;
  private ApplicationEventPublisher applicationEventPublisher;
  private MQTTMessageMapper mqttMessageMapper;
  private LeaderLatch leaderLatch;

  public ManagedMqttClient(final String ipAddress, final String clientName,
    final String topicTelemetry, final String topicMetadata,
    final ApplicationEventPublisher applicationEventPublisher,
    final MQTTMessageMapper mqttMessageMapper, String topicControlRequest,
    String topicControlReply) {
    this.ipAddress = ipAddress;
    this.clientName = clientName;
    this.topicMetadata = topicMetadata;
    this.topicTelemetry = topicTelemetry;
    this.applicationEventPublisher = applicationEventPublisher;
    this.mqttMessageMapper = mqttMessageMapper;
    this.topicControlRequest = topicControlRequest;
    this.topicControlReply = topicControlReply;
  }

  protected void connect() throws MqttException {
    // Connect to the MQTT server.
    client = new MqttClient(ipAddress, clientName, new MemoryPersistence());
    MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(true);
    client.connect(options);

    LOGGER.log(Level.FINE, "Connected to MQTT server {0}.", ipAddress);
  }

  protected void disconnect() throws IOException {
    LOGGER.log(Level.FINE, "Disconnecting from MQTT server {0}.", ipAddress);

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
        LOGGER.log(Level.SEVERE, MessageFormat.format("Could not disconnect from MQTT server {0}.",
          ipAddress), e);
      }
    }

    LOGGER.log(Level.FINE, "Disconnected from MQTT server {0}.", ipAddress);
  }

  /**
   * Subscribe to MQTT topics.
   */
  protected void subscribe() throws MqttException {
    LOGGER.log(Level.FINE, "Subscribing to {0} on MQTT server {1}.",
      new Object[]{StringUtils.joinWith(", ", topicTelemetry, topicMetadata, topicControlReply),
        ipAddress});

    client.subscribe(topicTelemetry + "/#", (topic, message) ->
      applicationEventPublisher.publishEvent(
        mqttMessageMapper.map(message)
          .setTopic(topic)
          .setEventType(EventType.TELEMETRY)
          .setHardwareId(topic.substring(topic.lastIndexOf('/') + 1))
          .setId(UUID.randomUUID().toString())));

    client.subscribe(topicMetadata + "/#", (topic, message) ->
      applicationEventPublisher.publishEvent(
        mqttMessageMapper.map(message)
          .setTopic(topic)
          .setEventType(EventType.METADATA)
          .setHardwareId(topic.substring(topic.lastIndexOf('/') + 1))
          .setId(UUID.randomUUID().toString())));

    client.subscribe(topicControlReply + "/#", (topic, message) ->
      applicationEventPublisher.publishEvent(
        mqttMessageMapper.map(message)
          .setTopic(topic)
          .setEventType(EventType.CONTROL_REPLY)
          .setHardwareId(topic.substring(topic.lastIndexOf('/') + 1))
          .setId(UUID.randomUUID().toString())));
  }

  protected void unsubscribe() throws MqttException {
    client.unsubscribe(
      new String[]{topicTelemetry + "/#", topicMetadata + "/#", topicControlReply + "/#"});
  }

  protected void participateInLeaderElection(long mqttServerId, CuratorFramework curatorFramework)
  throws Exception {
    curatorFramework.checkExists().creatingParentsIfNeeded()
      .forPath(LEADER_ELECTION_PATH_MQTT + "/" + mqttServerId);

    // Participate in leader election.
    LOGGER.log(Level.FINE, "Requesting participation in MQTT leader election for {0}.", ipAddress);
    leaderLatch = new LeaderLatch(curatorFramework,
      LEADER_ELECTION_PATH_MQTT + "/" + mqttServerId, clientName);

    // Add listeners for leadership status change.
    leaderLatch.addListener(new LeaderLatchListener() {
      @Override
      public void isLeader() {
        LOGGER.log(Level.FINEST, "Became MQTT leader for {0}.", ipAddress);
        try {
          subscribe();
        } catch (MqttException e) {
          LOGGER
            .log(Level.SEVERE, MessageFormat.format("Could not subscribe to MQTT topics on server "
              + "{0}.", ipAddress), e);
        }
      }

      @Override
      public void notLeader() {
        LOGGER.log(Level.FINEST, "Lost MQTT leadership for {0}.", ipAddress);
        try {
          unsubscribe();
        } catch (MqttException e) {
          LOGGER
            .log(Level.SEVERE, MessageFormat.format("Could not unsubscribe from MQTT topics on "
              + "server {0}.", ipAddress), e);
        }
      }
    });

    leaderLatch.start();
    LOGGER.log(Level.FINE, "Established participation in MQTT leader election for {0}.", ipAddress);
  }
}
