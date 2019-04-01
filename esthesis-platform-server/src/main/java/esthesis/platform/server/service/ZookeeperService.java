package esthesis.platform.server.service;

import esthesis.platform.server.config.AppConstants.Event;
import esthesis.platform.common.config.AppConstants.Generic;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.ZookeeperServerDTO;
import esthesis.platform.server.events.ZookeeperConfigurationChangedEvent;
import esthesis.platform.server.events.ZookeeperConnectivityEvent;
import esthesis.platform.server.events.ZookeeperConnectivityEvent.EVENT_TYPE;
import esthesis.platform.server.mapper.ZookeeperServerMapper;
import esthesis.platform.server.model.ZookeeperServer;
import esthesis.platform.server.repository.ZookeeperServerRepository;
import javax.annotation.PostConstruct;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Transactional
@Validated
public class ZookeeperService extends BaseService<ZookeeperServerDTO, ZookeeperServer> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ZookeeperService.class.getName());

  private final ZookeeperServerMapper zookeeperServerMapper;
  private final ZookeeperServerRepository zookeeperServerRepository;
  private final ZookeeperClientService zookeeperClientService;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final AppProperties appProperties;

  public ZookeeperService(ZookeeperServerMapper zookeeperServerMapper,
      ZookeeperServerRepository zookeeperServerRepository,
      ZookeeperClientService zookeeperClientService,
      ApplicationEventPublisher applicationEventPublisher, AppProperties appProperties) {
    this.zookeeperServerMapper = zookeeperServerMapper;
    this.zookeeperServerRepository = zookeeperServerRepository;
    this.zookeeperClientService = zookeeperClientService;
    this.applicationEventPublisher = applicationEventPublisher;
    this.appProperties = appProperties;
  }

  private void reconnect() {
    // Disconnect from existing Zookeeper and establish a new connection.
    try {
      zookeeperClientService.disconnect();
      connect();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not reconnect to Zookeeper.", e);
    }
  }

  private void publishZookeeperServerConfigurationChange(
      ZookeeperConfigurationChangedEvent zookeeperConfigurationChangedEvent)
      throws Exception {
    getZookeeperClient().setData()
        .forPath(Event.ZOOKEEPER_CONFIGURATION_EVENT_PATH, zookeeperConfigurationChangedEvent.toByteArray());
  }

  public void releaseLeadership() throws Exception {
    // Release leadership.
    LOGGER.log(Level.FINE, "Releasing leadership for Zookeeper.");
    zookeeperClientService.disconnect();
    connect();
  }

  public List<ZookeeperServerDTO> findActive() {
    return zookeeperServerMapper.map(zookeeperServerRepository.findAllByState(Generic.ENABLED));
  }

  @Async
  @PostConstruct
  public void connect() {
    final Optional<ZookeeperServerDTO> zookeeperServers = findActive().stream().findAny();
    if (!zookeeperServers.isPresent()) {
      LOGGER.log(Level.CONFIG, "No active Zookeeper servers configured, running in standalone mode.");
      applicationEventPublisher.publishEvent(new ZookeeperConnectivityEvent(EVENT_TYPE.CONNECTED));
    } else {
      LOGGER.log(Level.CONFIG, "Active Zookeeper servers configured, running in cluster mode.");
      ZookeeperServerDTO zookeeperServerDTO = zookeeperServers.get();
      zookeeperClientService.connect(zookeeperServerDTO);
    }
  }

  public void disconnect() throws IOException {
    zookeeperClientService.disconnect();
  }

  @Override
  public ZookeeperServerDTO save(ZookeeperServerDTO dto) {
    final ZookeeperServerDTO zookeeperServerDTOBefore = findById(dto.getId());
    // Save Zookeeper server configuration.
    dto = super.save(dto);

    reconnect();

    if (isCluster()) {
      LOGGER.log(Level.FINEST, "Publishing Zookeeper configuration change event.");
      try {
        publishZookeeperServerConfigurationChange(new ZookeeperConfigurationChangedEvent()
            .setZookeeperServerId(dto.getId())
            .setNodeInitiatingChange(appProperties.getNodeId())
            .setStateAfter(dto.getState())
            .setDeleted(false)
            .setStateBefore(zookeeperServerDTOBefore.getState()));
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Could not publish Zookeeper configuration change event.", e);
      }
    }

    // Return the object saved.
    return dto;
  }

  @Override
  public ZookeeperServerDTO deleteById(long id) {
    final ZookeeperServerDTO zookeeperServerDTOBefore = findById(id);

    // Delete Zookeeper server.
    final ZookeeperServerDTO zookeeperServerDTO = super.deleteById(id);

    reconnect();
    if (isCluster()) {
      LOGGER.log(Level.FINEST, "Publishing Zookeeper configuration change event.");
      try {
        publishZookeeperServerConfigurationChange(new ZookeeperConfigurationChangedEvent()
            .setZookeeperServerId(id)
            .setNodeInitiatingChange(appProperties.getNodeId())
            .setDeleted(true)
            .setStateBefore(zookeeperServerDTOBefore.getState()));
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Could not publish Zookeeper configuration change event.", e);
      }
    }

    // Return the object deleted.
    return zookeeperServerDTO;
  }

  public boolean isLeader() {
    return zookeeperClientService.isLeader();
  }

  public boolean isCluster() {
    return zookeeperClientService.isCluster();
  }

  public String getConnectionString() {
    return zookeeperClientService.getConnectionString();
  }

  public CuratorFramework getZookeeperClient() {
    return zookeeperClientService.getZookeeperClient();
  }
}
