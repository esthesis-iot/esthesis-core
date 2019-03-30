package esthesis.platform.server.service;

import static esthesis.platform.server.config.AppConstants.Zookeeper.CONNECT_RETRY;
import static esthesis.platform.server.config.AppConstants.Zookeeper.LEADER_ELECTION_PATH_GLOBAL;

import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.ZookeeperServerDTO;
import esthesis.platform.server.events.ZookeeperConnectivityEvent;
import esthesis.platform.server.events.ZookeeperConnectivityEvent.EVENT_TYPE;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatch.CloseMode;
import org.apache.curator.framework.recipes.leader.LeaderLatch.State;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A service encapsulating the connection to Zookeeper. It is strongly advised that services requiring access to
 * Zookeeper do so by using the methods provided by {@link ZookeeperService} and not by directly using this service.
 */
@Service
public class ZookeeperClientService {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ZookeeperClientService.class.getName());
  private CuratorFramework zookeeperClient;
  // Global cluster leader latch.
  private LeaderLatch leaderLatch;
  private final AppProperties appProperties;
  private final ApplicationEventPublisher applicationEventPublisher;

  public ZookeeperClientService(AppProperties appProperties,
      ApplicationEventPublisher applicationEventPublisher) {
    this.appProperties = appProperties;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void disconnect() throws IOException {
    if (leaderLatch != null && leaderLatch.getState() == State.STARTED) {
      leaderLatch.close(CloseMode.NOTIFY_LEADER);
      leaderLatch = null;
    }

    if (zookeeperClient != null && zookeeperClient.getState() == CuratorFrameworkState.STARTED) {
      LOGGER.log(Level.CONFIG, "Disconnecting from Zookeeper server {0}.", getConnectionString());
      zookeeperClient.close();
      zookeeperClient = null;
    }

    applicationEventPublisher.publishEvent(new ZookeeperConnectivityEvent(EVENT_TYPE.DISCONNECTED));
  }

  public String getConnectionString() {
    if (zookeeperClient != null) {
      return zookeeperClient.getZookeeperClient().getCurrentConnectionString();
    } else {
      return null;
    }
  }

  @Async
  public void connect(ZookeeperServerDTO zookeeperServerDTO) {
    try {
      // Connect to the Zookeeper server.
      LOGGER.log(Level.FINE, "Connecting to Zookeeper at {0}.", zookeeperServerDTO.getIpAddress());
      zookeeperClient = CuratorFrameworkFactory.builder()
          .connectString(zookeeperServerDTO.getIpAddress())
          .retryPolicy(new ExponentialBackoffRetry(1000, CONNECT_RETRY))
          .build();
      zookeeperClient.start();
      LOGGER.log(Level.FINE, "Connected to Zookeeper at {0}.", zookeeperServerDTO.getIpAddress());

      // Participate in leader election.
      zookeeperClient.checkExists().creatingParentsIfNeeded().forPath(LEADER_ELECTION_PATH_GLOBAL);
      LOGGER.fine("Participating in global leader election.");
      leaderLatch = new LeaderLatch(zookeeperClient, LEADER_ELECTION_PATH_GLOBAL, appProperties.getNodeId());

      // Add listeners for leadership status change.
      leaderLatch.addListener(new LeaderLatchListener() {
        @Override
        public void isLeader() {
          LOGGER.log(Level.FINEST, "Became global cluster leader.");
          applicationEventPublisher
              .publishEvent(new ZookeeperConnectivityEvent(EVENT_TYPE.CLUSTER_LEADER, zookeeperServerDTO.getId()));
        }

        @Override
        public void notLeader() {
          LOGGER.log(Level.FINEST, "Lost global cluster leadership.");
          applicationEventPublisher
              .publishEvent(new ZookeeperConnectivityEvent(EVENT_TYPE.CLUSTER_FOLLOWER, zookeeperServerDTO.getId()));
        }
      });

      leaderLatch.start();
      applicationEventPublisher.publishEvent(new ZookeeperConnectivityEvent(EVENT_TYPE.CONNECTED, zookeeperServerDTO.getId()));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, MessageFormat.format("Could not connect to Zookeeper server: {0}, due to {1}.",
          zookeeperServerDTO.ipAddress, e.getMessage()), e);
    }
  }

  public CuratorFramework getZookeeperClient() {
    return zookeeperClient;
  }

  public boolean isLeader() {
    if (leaderLatch != null) {
      return leaderLatch.hasLeadership();
    } else {
      return false;
    }
  }

  public boolean isCluster() {
    return leaderLatch != null;
  }

}
