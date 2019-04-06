package esthesis.platform.server.service;

import esthesis.extension.platform.event.MQTTDataEvent;
import esthesis.extension.platform.event.MQTTMetadataEvent;
import esthesis.extension.platform.event.MQTTTelemetryEvent;
import esthesis.extension.platform.sink.EsthesisDataSinkFactory;
import esthesis.extension.platform.sink.EsthesisMetadataSink;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;
import esthesis.platform.server.config.AppConstants.Event;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.datasinks.DataSinkMessenger;
import esthesis.platform.server.datasinks.DataSinkScanner;
import esthesis.platform.server.dto.DataSinkDTO;
import esthesis.platform.server.dto.DataSinkFactoryDTO;
import esthesis.platform.server.events.DataSinkConfigurationChangedEvent;
import esthesis.platform.server.mapper.DataSinkMapper;
import esthesis.platform.server.model.DataSink;
import esthesis.platform.server.repository.DataSinkRepository;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Validated
@Transactional
public class DataSinkService extends BaseService<DataSinkDTO, DataSink> implements ApplicationListener<MQTTDataEvent> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DataSinkService.class.getName());

  private final DataSinkRepository dataSinkRepository;
  private final DataSinkMapper dataSinkMapper;
  private final DataSinkScanner dataSinkScanner;
  private final ZookeeperService zookeeperService;
  private final AppProperties appProperties;
  private final DataSinkMessenger dataSinkMessenger;
  // List of available and active metadata data sinks.
  public final Map<Long, EsthesisMetadataSink> activeMetadataSinks = new HashMap<>();
  // List of available telemetry data sinks.
  public final Map<Long, EsthesisTelemetrySink> activeTelemetrySinks = new HashMap<>();

  public DataSinkService(DataSinkRepository dataSinkRepository,
      DataSinkMapper dataSinkMapper, DataSinkScanner dataSinkScanner,
      ZookeeperService zookeeperService, AppProperties appProperties,
      DataSinkMessenger dataSinkMessenger) {
    this.dataSinkRepository = dataSinkRepository;
    this.dataSinkMapper = dataSinkMapper;
    this.dataSinkScanner = dataSinkScanner;
    this.zookeeperService = zookeeperService;
    this.appProperties = appProperties;
    this.dataSinkMessenger = dataSinkMessenger;
  }

  private void publishDataSinkConfigurationChange(DataSinkConfigurationChangedEvent dataSinkConfigurationChangedEvent)
      throws Exception {
    zookeeperService.getZookeeperClient().setData()
        .forPath(Event.DATA_SINK_CONFIGURATION_EVENT_PATH, dataSinkConfigurationChangedEvent.toByteArray());
  }

  @PostConstruct
  public void initiateActiveSinks() {
    LOGGER.log(Level.FINEST, "Initializing active data sinks.");
    // Get all registered active metadata sinks.
    findActiveMetadataSinks().forEach(dataSink -> {
      if (!activeMetadataSinks.containsKey(dataSink.getId())) {
        try {
          // Create an instance for this data sink.
          final EsthesisDataSinkFactory esthesisDataSinkFactory =
              (EsthesisDataSinkFactory) Class.forName(dataSink.getFactoryClass()).newInstance();
          // Set the configuration to the data sink.
          esthesisDataSinkFactory.setConfiguration(dataSink.getConfiguration());
          // Keep the instance of this data sink to a local map.
          activeMetadataSinks.put(dataSink.getId(), esthesisDataSinkFactory.getMetadataSink());
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE,
              MessageFormat.format("Could not instantiate metadata data sink {0}.", dataSink.getFactoryClass()), e);
        }
      }
    });

    // Get all registered active telemetry sinks.
    findActiveTelemetrySinks().forEach(dataSink -> {
      if (!activeTelemetrySinks.containsKey(dataSink.getId())) {
        try {
          // Create an instance for this data sink.
          final EsthesisDataSinkFactory esthesisDataSinkFactory =
              (EsthesisDataSinkFactory) Class.forName(dataSink.getFactoryClass()).newInstance();
          // Set the configuration to the data sink.
          esthesisDataSinkFactory.setConfiguration(dataSink.getConfiguration());
          // Keep the instance of this data sink to a local map.
          activeTelemetrySinks.put(dataSink.getId(), esthesisDataSinkFactory.getTelemetrySink());
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE,
              MessageFormat.format("Could not instantiate telemetry data sink {0}.", dataSink.getFactoryClass()), e);
        }
      }
    });
  }

  @PreDestroy
  public void stop() {
    activeMetadataSinks.entrySet().parallelStream().forEach(dataSyncEntry
        -> dataSyncEntry.getValue().disconnect());
    activeTelemetrySinks.entrySet().parallelStream().forEach(dataSyncEntry
        -> dataSyncEntry.getValue().disconnect());
  }

  public List<DataSinkDTO> findActiveMetadataSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndMetadata(true, true));
  }

  public List<DataSinkDTO> findActiveTelemetrySinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndTelemetry(true, true));
  }

  public List<DataSinkFactoryDTO> findAvailableDataSinkFactories() {
    return dataSinkScanner.getAvailableDataSinkFactories();
  }

  @Override
  public DataSinkDTO save(DataSinkDTO dto) {
    DataSinkDTO dataSinkDTOBefore = null;
    // If this data sink existed before stop it to restart it after save.
    if (dto.getId() != 0) {
      dataSinkDTOBefore = findById(dto.getId());
      final EsthesisMetadataSink esthesisMetadataSink = activeMetadataSinks.get(dto.getId());
      if (esthesisMetadataSink != null) {
        esthesisMetadataSink.disconnect();
        activeMetadataSinks.remove(dto.getId());
      }
      final EsthesisTelemetrySink esthesisTelemetrySink = activeTelemetrySinks.get(dto.getId());
      if (esthesisTelemetrySink != null) {
        esthesisTelemetrySink.disconnect();
        activeTelemetrySinks.remove(dto.getId());
      }
    }

    // Save the data sink.
    final DataSinkDTO dataSinkDTO = super.save(dto);

    // Re-initiate data sinks.
    initiateActiveSinks();

    if (zookeeperService.isCluster()) {
      LOGGER.log(Level.FINEST, "Publishing Data sink configuration change event.");
      try {
        publishDataSinkConfigurationChange(new DataSinkConfigurationChangedEvent()
            .setDataSinkId(dto.getId())
            .setNodeInitiatingChange(appProperties.getNodeId())
            .setStateAfter(dto.getState())
            .setDeleted(false)
            .setStateBefore(dataSinkDTOBefore != null ? dataSinkDTOBefore.getState() : false));
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Could not publish Zookeeper configuration change event.", e);
      }
    }

    return dataSinkDTO;
  }

  @Override
  public void onApplicationEvent(MQTTDataEvent event) {
    LOGGER.log(Level.FINEST, "Starting distribution of MQTT event {0} on topic {1} for device {2}.",
        new Object[]{event.getId(), event.getTopic(), event.getDeviceId()});
    // Pass events to data sinks only if this node is a cluster leader (or in standalone mode).
    if (!zookeeperService.isCluster() || zookeeperService.isLeader()) {
      if (event instanceof MQTTMetadataEvent) {
        activeMetadataSinks.entrySet().parallelStream().forEach(
            dataSinkEntry -> dataSinkMessenger.metadataMessage(dataSinkEntry.getValue(), event));
      } else if (event instanceof MQTTTelemetryEvent) {
        activeTelemetrySinks.entrySet().parallelStream().forEach(
            dataSinkEntry -> dataSinkMessenger.telemetryMessage(dataSinkEntry.getValue(), event));
      }
    }
    LOGGER.log(Level.FINEST, "Finished distribution of MQTT event {0} on topic {1} for device {2}.",
        new Object[]{event.getId(), event.getTopic(), event.getDeviceId()});
  }
}
