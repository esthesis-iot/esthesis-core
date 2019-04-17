package esthesis.platform.server.cluster.datasinks;

import esthesis.extension.platform.event.MQTTDataEvent;
import esthesis.extension.platform.event.MQTTMetadataEvent;
import esthesis.extension.platform.event.MQTTTelemetryEvent;
import esthesis.extension.platform.sink.EsthesisDataSinkFactory;
import esthesis.extension.platform.sink.EsthesisMetadataSink;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;
import esthesis.platform.server.cluster.ClusterInfoService;
import esthesis.platform.server.cluster.zookeeper.ZookeeperClientManager;
import esthesis.platform.server.config.AppConstants.Zookeeper;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.datasinks.DataSinkMessenger;
import esthesis.platform.server.dto.DataSinkDTO;
import esthesis.platform.server.events.ClusterEvent;
import esthesis.platform.server.events.ClusterEvent.CLUSTER_EVENT_TYPE;
import esthesis.platform.server.events.LocalEvent;
import esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE;
import esthesis.platform.server.mapper.DataSinkMapper;
import esthesis.platform.server.repository.DataSinkRepository;
import javax.annotation.PreDestroy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DataSinkManager {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DataSinkManager.class.getName());
  private final DataSinkRepository dataSinkRepository;
  private final DataSinkMapper dataSinkMapper;
  private final DataSinkMessenger dataSinkMessenger;
  private final ClusterInfoService clusterInfoService;
  private final ZookeeperClientManager zookeeperClientManager;
  private final AppProperties appProperties;

  public DataSinkManager(DataSinkRepository dataSinkRepository, DataSinkMapper dataSinkMapper,
    DataSinkMessenger dataSinkMessenger,
    ClusterInfoService clusterInfoService,
    ZookeeperClientManager zookeeperClientManager,
    AppProperties appProperties) {
    this.dataSinkRepository = dataSinkRepository;
    this.dataSinkMapper = dataSinkMapper;
    this.dataSinkMessenger = dataSinkMessenger;
    this.clusterInfoService = clusterInfoService;
    this.zookeeperClientManager = zookeeperClientManager;
    this.appProperties = appProperties;
  }

  // List of available and active metadata data sinks.
  private final Map<Long, EsthesisMetadataSink> startedMetadataSinks = new HashMap<>();
  // List of available telemetry data sinks.
  private final Map<Long, EsthesisTelemetrySink> startedTelemetrySinks = new HashMap<>();

  private List<DataSinkDTO> findActiveMetadataSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndMetadata(true, true));
  }

  private List<DataSinkDTO> findActiveTelemetrySinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndTelemetry(true, true));
  }

  public void startDataSinks() {
    LOGGER.log(Level.FINEST, "Initializing active data sinks.");
    // Start all active metadata sinks.
    findActiveMetadataSinks().forEach(dataSink -> {
      try {
        // Create an instance for this data sink.
        final EsthesisDataSinkFactory esthesisDataSinkFactory =
          (EsthesisDataSinkFactory) Class.forName(dataSink.getFactoryClass()).newInstance();
        // Set the configuration to the data sink.
        esthesisDataSinkFactory.setConfiguration(dataSink.getConfiguration());
        // Keep the instance of this data sink to a local map.
        startedMetadataSinks.put(dataSink.getId(), esthesisDataSinkFactory.getMetadataSink());
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, MessageFormat
          .format("Could not instantiate metadata data sink {0}.", dataSink.getFactoryClass()), e);
      }
    });

    // Start all active telemetry sinks.
    findActiveTelemetrySinks().forEach(dataSink -> {
      try {
        // Create an instance for this data sink.
        final EsthesisDataSinkFactory esthesisDataSinkFactory =
          (EsthesisDataSinkFactory) Class.forName(dataSink.getFactoryClass()).newInstance();
        // Set the configuration to the data sink.
        esthesisDataSinkFactory.setConfiguration(dataSink.getConfiguration());
        // Keep the instance of this data sink to a local map.
        startedTelemetrySinks.put(dataSink.getId(), esthesisDataSinkFactory.getTelemetrySink());
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE,
          MessageFormat
            .format("Could not instantiate telemetry data sink {0}.", dataSink.getFactoryClass()),
          e);
      }
    });
  }

  @PreDestroy
  public void stopDataSinks() {
    startedMetadataSinks.entrySet().parallelStream().forEach(dataSyncEntry
      -> dataSyncEntry.getValue().disconnect());
    startedTelemetrySinks.entrySet().parallelStream().forEach(dataSyncEntry
      -> dataSyncEntry.getValue().disconnect());
  }

  /**
   * Persist MQTT data to the data sinks. MQTTDataEvents are received, by default, only when the
   * node is a cluster leader.
   */
  @EventListener
  public void onApplicationEvent(MQTTDataEvent event) {
    LOGGER.log(Level.FINEST, "Starting distribution of MQTT event {0} on topic {1} for device {2}.",
      new Object[]{event.getId(), event.getTopic(), event.getHardwareId()});
    if (event instanceof MQTTMetadataEvent) {
      startedMetadataSinks.entrySet().parallelStream().forEach(
        dataSinkEntry -> dataSinkMessenger.metadataMessage(dataSinkEntry.getValue(), event));
    } else if (event instanceof MQTTTelemetryEvent) {
      startedTelemetrySinks.entrySet().parallelStream().forEach(
        dataSinkEntry -> dataSinkMessenger.telemetryMessage(dataSinkEntry.getValue(), event));
    }
    LOGGER.log(Level.FINEST, "Finished distribution of MQTT event {0} on topic {1} for device {2}.",
      new Object[]{event.getId(), event.getTopic(), event.getHardwareId()});
  }

  /**
   * Handle data sink configuration changes.
   */
  @EventListener
  public void onApplicationEvent(LocalEvent event) {
    if (event.getEventType().equals(LOCAL_EVENT_TYPE.CONFIGURATION_DATASINK)) {
      LOGGER.log(Level.FINEST, "EVENT: {0}.", event);
      stopDataSinks();
      startDataSinks();
      if (!clusterInfoService.isStandalone() && !event.isClusterEvent()) {
        try {
          zookeeperClientManager.getZookeeperClient().setData()
            .forPath(Zookeeper.DATA_SINK_CONFIGURATION_EVENT_PATH,
              new ClusterEvent(CLUSTER_EVENT_TYPE.CONFIGURATION_DATA_SINK)
                .setEmitterNode(appProperties.getNodeId()).toByteArray());
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Could not process PlatformEvent.", e);
        }
      }
    }
  }
}
