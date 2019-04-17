package esthesis.platform.server.cluster.zookeeper;

import static esthesis.platform.server.config.AppConstants.Zookeeper.CONNECT_RETRY;
import static esthesis.platform.server.config.AppConstants.Zookeeper.DATA_SINK_CONFIGURATION_EVENT_PATH;
import static esthesis.platform.server.config.AppConstants.Zookeeper.LEADER_ELECTION_PATH_GLOBAL;
import static esthesis.platform.server.config.AppConstants.Zookeeper.MQTT_CONFIGURATION_EVENT_PATH;
import static esthesis.platform.server.config.AppConstants.Zookeeper.ZOOKEEPER_CONFIGURATION_EVENT_PATH;
import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONFIGURATION_DATASINK;
import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONFIGURATION_MQTT;
import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONFIGURATION_ZOOKEEPER;
import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONNECTIVITY_ZOOKEEPER_FOLLOWER;
import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONNECTIVITY_ZOOKEEPER_LEADER;

import esthesis.platform.server.cluster.ClusterInfoService;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.ZookeeperServerDTO;
import esthesis.platform.server.events.ClusterEvent;
import esthesis.platform.server.events.LocalEvent;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatch.CloseMode;
import org.apache.curator.framework.recipes.leader.LeaderLatch.State;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A component encapsulating the connection to Zookeeper. It is strongly advised that services
 * requiring access to Zookeeper do so by using the methods provided by {@link
 * ZookeeperClientManager} and not by directly using this component.
 */
@Component
public class ManagedZookeeperClient {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ManagedZookeeperClient.class.getName());
  private CuratorFramework curatorFramework;
  // Global cluster leader latch.
  private LeaderLatch leaderLatch;
  private final AppProperties appProperties;
  private final ApplicationEventPublisher applicationEventPublisher;
  // Zookeeper event listener for MQTT configuration changes.
  private NodeCache mqttNodeCache;
  // Zookeeper event listener for Zookeeper configuration changes.
  private NodeCache zookeeperNodeCache;
  // Zookeeper event listener for data sink configuration changes.
  private NodeCache dataSinkNodeCache;
  private final ClusterInfoService clusterInfoService;

  public ManagedZookeeperClient(AppProperties appProperties,
    ApplicationEventPublisher applicationEventPublisher,
    ClusterInfoService clusterInfoService) {
    this.appProperties = appProperties;
    this.applicationEventPublisher = applicationEventPublisher;
    this.clusterInfoService = clusterInfoService;
  }

  protected void disconnect() throws IOException {
    // Stop listening for cluster events.
    tearDownClusterEventListener();

    if (leaderLatch != null && leaderLatch.getState() == State.STARTED) {
      leaderLatch.close(CloseMode.NOTIFY_LEADER);
      leaderLatch = null;
    }

    if (curatorFramework != null && curatorFramework.getState() == CuratorFrameworkState.STARTED) {
      LOGGER.log(Level.CONFIG, "Disconnecting from Zookeeper server {0}.", getConnectionString());
      curatorFramework.close();
      curatorFramework = null;
    }
  }

  protected String getConnectionString() {
    if (curatorFramework != null) {
      return curatorFramework.getZookeeperClient().getCurrentConnectionString();
    } else {
      return null;
    }
  }

  /**
   * Listen for cluster events and translate them to local events.
   */
  @SuppressWarnings("Duplicates")
  private void setupClusterEventListener() {
    try {
      // Setup monitors for MQTT configuration changes.
      LOGGER.log(Level.FINE, "Starting cluster monitor for MQTT configuration changes.");
      curatorFramework.createContainers(MQTT_CONFIGURATION_EVENT_PATH);
      mqttNodeCache = new NodeCache(curatorFramework, MQTT_CONFIGURATION_EVENT_PATH);
      mqttNodeCache.getListenable().addListener(() -> {
        final ClusterEvent clusterEvent = ClusterEvent.fromByteArray(curatorFramework.getData()
          .forPath(MQTT_CONFIGURATION_EVENT_PATH));
        LOGGER.log(Level.FINEST, "CLUSTER EVENT: {0}.", clusterEvent);
        // Handle the event, skipping events generated by this node.
        if (!clusterEvent.getEmitterNode().equals(appProperties.getNodeId())) {
          applicationEventPublisher.publishEvent(
            new LocalEvent(CONFIGURATION_MQTT).setClusterEvent(true)
          );
        } else {
          LOGGER.log(Level.FINEST, "Skipping self-generated event.");
        }
      });
      mqttNodeCache.start(true);
      LOGGER.log(Level.FINE, "Cluster monitor for MQTT configuration changes started.");

      // Setup monitors for Zookeeper configuration changes.
      LOGGER.log(Level.FINE, "Starting cluster monitor for Zookeeper configuration changes.");
      curatorFramework.createContainers(ZOOKEEPER_CONFIGURATION_EVENT_PATH);
      zookeeperNodeCache = new NodeCache(curatorFramework, ZOOKEEPER_CONFIGURATION_EVENT_PATH);
      zookeeperNodeCache.getListenable().addListener(() -> {
        final ClusterEvent clusterEvent = ClusterEvent
          .fromByteArray(curatorFramework.getData().forPath(ZOOKEEPER_CONFIGURATION_EVENT_PATH));
        LOGGER.log(Level.FINEST, "CLUSTER EVENT {0}.", clusterEvent);
        // Handle the event, skipping events generated by this node.
        if (!clusterEvent.getEmitterNode().equals(appProperties.getNodeId())) {
          applicationEventPublisher.publishEvent(
            new LocalEvent(CONFIGURATION_ZOOKEEPER).setClusterEvent(true)
          );
        } else {
          LOGGER.log(Level.FINEST, "Skipping self-generated event.");
        }
      });
      zookeeperNodeCache.start(true);
      LOGGER.log(Level.FINE, "Cluster monitor for Zookeeper configuration changes started.");

      // Setup monitors for data sink configuration changes.
      LOGGER.log(Level.FINE, "Starting cluster monitor for data sink configuration changes.");
      curatorFramework.createContainers(DATA_SINK_CONFIGURATION_EVENT_PATH);
      dataSinkNodeCache = new NodeCache(curatorFramework, DATA_SINK_CONFIGURATION_EVENT_PATH);
      dataSinkNodeCache.getListenable().addListener(() -> {
        final ClusterEvent clusterEvent = ClusterEvent
          .fromByteArray(curatorFramework.getData().forPath(DATA_SINK_CONFIGURATION_EVENT_PATH));
        LOGGER.log(Level.FINEST, "CLUSTER EVENT {0}.", clusterEvent);
        // Handle the event, skipping events generated by this node.
        if (!clusterEvent.getEmitterNode().equals(appProperties.getNodeId())) {
          applicationEventPublisher.publishEvent(
            new LocalEvent(CONFIGURATION_DATASINK).setClusterEvent(true)
          );
        } else {
          LOGGER.log(Level.FINEST, "Skipping self-generated event.");
        }
      });
      dataSinkNodeCache.start(true);
      LOGGER.log(Level.FINE, "Cluster monitor for data sink configuration changes started.");
    } catch (Exception e) {
      LOGGER
        .log(Level.SEVERE, "Could not establish monitors for cluster configuration changes.", e);
    }
  }

  /**
   * Stops all cluster event listeners.
   */
  private void tearDownClusterEventListener() {
    try {
      if (mqttNodeCache != null) {
        LOGGER.log(Level.FINE, "Closing MQTT configuration changes cluster monitor.");
        mqttNodeCache.close();
        mqttNodeCache = null;

        LOGGER.log(Level.FINE, "Closing Zookeeper configuration changes cluster monitor.");
        zookeeperNodeCache.close();
        zookeeperNodeCache = null;

        LOGGER.log(Level.FINE, "Closing data sink configuration changes cluster monitor.");
        dataSinkNodeCache.close();
        dataSinkNodeCache = null;
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not close monitors for cluster configuration changes.", e);
    }
  }

  protected void connect(ZookeeperServerDTO zookeeperServerDTO) {
    try {
      // Connect to the Zookeeper server.
      LOGGER.log(Level.FINE, "Connecting to Zookeeper at {0}.", zookeeperServerDTO.getIpAddress());
      curatorFramework = CuratorFrameworkFactory.builder()
        .connectString(zookeeperServerDTO.getIpAddress())
        .retryPolicy(new ExponentialBackoffRetry(1000, CONNECT_RETRY))
        .build();
      curatorFramework.start();
      LOGGER.log(Level.FINE, "Connected to Zookeeper at {0}.", zookeeperServerDTO.getIpAddress());

      // Participate in leader election.
      curatorFramework.checkExists().creatingParentsIfNeeded().forPath(LEADER_ELECTION_PATH_GLOBAL);
      LOGGER.fine("Participating in global leader election.");
      leaderLatch = new LeaderLatch(curatorFramework, LEADER_ELECTION_PATH_GLOBAL,
        appProperties.getNodeId());

      // Add listeners for leadership status change.
      leaderLatch.addListener(new LeaderLatchListener() {
        @Override
        public void isLeader() {
          LOGGER.log(Level.FINEST, "Became global cluster leader.");
          applicationEventPublisher.publishEvent(new LocalEvent(CONNECTIVITY_ZOOKEEPER_LEADER));
          clusterInfoService.setClusterLeader(true);
        }

        @Override
        public void notLeader() {
          LOGGER.log(Level.FINEST, "Lost global cluster leadership.");
          applicationEventPublisher.publishEvent(new LocalEvent(CONNECTIVITY_ZOOKEEPER_FOLLOWER));
          clusterInfoService.setClusterLeader(false);
        }
      });
      leaderLatch.start();

      // Add listeners for cluster event about configuration changes performed by other nodes.
      setupClusterEventListener();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE,
        MessageFormat.format("Could not connect to Zookeeper server: {0}, due to {1}.",
          zookeeperServerDTO.ipAddress, e.getMessage()), e);
    }
  }

  protected CuratorFramework getClient() {
    return curatorFramework;
  }

  //  protected boolean isLeader() {
  //    if (leaderLatch != null) {
  //      return leaderLatch.hasLeadership();
  //    } else {
  //      return false;
  //    }
  //  }
  //
  //  protected boolean isCluster() {
  //    return leaderLatch != null;
  //  }

}
