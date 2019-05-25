package esthesis.platform.server.cluster.zookeeper;

import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONNECTIVITY_ZOOKEEPER_CONNECTED;
import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONNECTIVITY_ZOOKEEPER_DISCONNECTED;

import esthesis.platform.server.cluster.ClusterInfoService;
import esthesis.platform.server.config.AppConstants.Zookeeper;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.ZookeeperServerDTO;
import esthesis.platform.server.events.ClusterEvent;
import esthesis.platform.server.events.ClusterEvent.CLUSTER_EVENT_TYPE;
import esthesis.platform.server.events.LocalEvent;
import esthesis.platform.server.mapper.ZookeeperServerMapper;
import esthesis.platform.server.model.ZookeeperServer;
import esthesis.platform.server.repository.ZookeeperServerRepository;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Validated
@Transactional
public class ZookeeperClientManager {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ZookeeperClientManager.class.getName());
  private final ZookeeperServerRepository zookeeperServerRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ManagedZookeeperClient zookeeperClient;
  private final ZookeeperServerMapper zookeeperServerMapper;
  private final AppProperties appProperties;
  private final ClusterInfoService clusterInfoService;

  public ZookeeperClientManager(
    ZookeeperServerRepository zookeeperServerRepository,
    ApplicationEventPublisher applicationEventPublisher,
    ManagedZookeeperClient zookeeperClient,
    ZookeeperServerMapper zookeeperServerMapper,
    AppProperties appProperties,
    ClusterInfoService clusterInfoService) {
    this.zookeeperServerRepository = zookeeperServerRepository;
    this.applicationEventPublisher = applicationEventPublisher;
    this.zookeeperClient = zookeeperClient;
    this.zookeeperServerMapper = zookeeperServerMapper;
    this.appProperties = appProperties;
    this.clusterInfoService = clusterInfoService;
  }

  public void releaseLeadership() throws Exception {
    // Release leadership.
    LOGGER.log(Level.FINE, "Releasing leadership for Zookeeper.");
    zookeeperClient.disconnect();
    connect();
  }

  @Async
  public void connect() {
    final Optional<ZookeeperServer> zookeeperServer =
      zookeeperServerRepository.findAllByState(true).stream().findAny();
    if (!zookeeperServer.isPresent()) {
      LOGGER
        .log(Level.CONFIG, "No active Zookeeper servers configured, running in standalone mode.");
      clusterInfoService.setStandalone(true);
    } else {
      LOGGER.log(Level.CONFIG, "Active Zookeeper servers configured, running in cluster mode.");
      ZookeeperServerDTO zookeeperServerDTO = zookeeperServerMapper.map(zookeeperServer.get());
      zookeeperClient.connect(zookeeperServerDTO);
      clusterInfoService.setStandalone(false);
    }

    // Emit an event about Zookeeper being connected.
    applicationEventPublisher.publishEvent(new LocalEvent(CONNECTIVITY_ZOOKEEPER_CONNECTED));
  }

  public void disconnect() throws IOException {
    if (!clusterInfoService.isStandalone()) {
      zookeeperClient.disconnect();
    }
    applicationEventPublisher.publishEvent(new LocalEvent(CONNECTIVITY_ZOOKEEPER_DISCONNECTED));
  }

  public CuratorFramework getZookeeperClient() {
    return zookeeperClient.getClient();
  }

  @EventListener
  public void onApplicationEvent(LocalEvent event) {
    LOGGER.log(Level.FINEST, "Received event {0}.", event);
    switch (event.getEventType()) {
      case CONFIGURATION_ZOOKEEPER:
        LOGGER.log(Level.FINEST, "Handling event: {0}.", event);
        try {
          // Since the configuration of the underlying Zookeeper nodes changed, try to establish a
          // new connection to a Zookeeper node.
          disconnect();
          connect();

          // If operating in a cluster, notify other cluster nodes to do the same.
          if (!clusterInfoService.isStandalone() && !event.isClusterEvent()) {
            getZookeeperClient().setData()
              .forPath(Zookeeper.ZOOKEEPER_CONFIGURATION_EVENT_PATH,
                new ClusterEvent(CLUSTER_EVENT_TYPE.CONFIGURATION_ZOOKEEPER)
                  .setEmitterNode(appProperties.getNodeId()).toByteArray());
          }
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, MessageFormat.format("Could not process event {0}.", event), e);
        }
    }
  }
}
