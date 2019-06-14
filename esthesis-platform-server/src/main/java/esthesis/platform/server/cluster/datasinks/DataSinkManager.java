package esthesis.platform.server.cluster.datasinks;

import com.eurodyn.qlack.common.exception.QMismatchException;
import esthesis.extension.datasink.DataSink;
import esthesis.extension.datasink.DataSinkFactory;
import esthesis.extension.datasink.MQTTDataEvent;
import esthesis.extension.datasink.config.AppConstants.Mqtt.EventType;
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
import esthesis.platform.server.service.DataSinkService;
import javax.annotation.PreDestroy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DataSinkManager {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DataSinkManager.class.getName());
  private final DataSinkMessenger dataSinkMessenger;
  private final ClusterInfoService clusterInfoService;
  private final ZookeeperClientManager zookeeperClientManager;
  private final AppProperties appProperties;
  private final DataSinkService dataSinkService;

  public DataSinkManager(DataSinkMessenger dataSinkMessenger, ClusterInfoService clusterInfoService,
    ZookeeperClientManager zookeeperClientManager,
    AppProperties appProperties, DataSinkService dataSinkService) {
    this.dataSinkMessenger = dataSinkMessenger;
    this.clusterInfoService = clusterInfoService;
    this.zookeeperClientManager = zookeeperClientManager;
    this.appProperties = appProperties;
    this.dataSinkService = dataSinkService;
  }

  // List of available and active metadata write data sinks.
  private final Map<Long, DataSink> startedMetadataWriteSinks = new HashMap<>();
  // List of available telemetry write data sinks.
  private final Map<Long, DataSink> startedTelemetryWriteSinks = new HashMap<>();
  // List of available and active metadata read data sinks.
  private final Map<Long, DataSink> startedMetadataReadSinks = new HashMap<>();
  // List of available telemetry data read sinks.
  private final Map<Long, DataSink> startedTelemetryReadSinks = new HashMap<>();

  private void initialiseDataSink(List<DataSinkDTO> dataSinks, String eventTypeHandler,
    Map<Long, DataSink> map) {
    dataSinks.forEach(dataSink -> {
      try {
        // Create an instance for this data sink.
        final DataSinkFactory esthesisDataSinkFactory =
          (DataSinkFactory) Class.forName(dataSink.getFactoryClass()).newInstance();
        // Set the configuration to the data sink.
        esthesisDataSinkFactory.setConfiguration(dataSink.getConfiguration());
        // Keep the instance of this data sink to a local map.
        if (eventTypeHandler.equals(EventType.METADATA)) {
          map.put(dataSink.getId(), esthesisDataSinkFactory.getMetadataSink());
          LOGGER.log(Level.FINE, "Started metadata write sink: {0}.", esthesisDataSinkFactory);
        } else if (eventTypeHandler.equals(EventType.TELEMETRY)) {
          map.put(dataSink.getId(), esthesisDataSinkFactory.getTelemetrySink());
          LOGGER.log(Level.FINE, "Started telemetry write sink: {0}.", esthesisDataSinkFactory);
        } else {
          throw new QMismatchException("Unspecified event type handler.");
        }
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, MessageFormat
          .format("Could not instantiate metadata write sink {0}.", dataSink.getFactoryClass()), e);
      }
    });
  }

  public Optional<DataSink> getMetadataWriter() {
    return startedMetadataWriteSinks.values().stream().findAny();
  }

  public Optional<DataSink> getMetadataReader() {
    return startedMetadataReadSinks.values().stream().findAny();
  }

  public Optional<DataSink> getTelemetryWriter() {
    return startedTelemetryWriteSinks.values().stream().findAny();
  }

  public Optional<DataSink> getTelemetryReader() {
    return startedTelemetryReadSinks.values().stream().findAny();
  }

  public void startDataSinks() {
    LOGGER.log(Level.FINEST, "Initializing active data sinks.");
    // Start all active metadata write sinks.
    initialiseDataSink(dataSinkService.findActiveMetadataWriteSinks(), EventType.METADATA,
      startedMetadataWriteSinks);
    initialiseDataSink(dataSinkService.findActiveTelemetryWriteSinks(), EventType.TELEMETRY,
      startedTelemetryWriteSinks);
    initialiseDataSink(dataSinkService.findActiveMetadataReadSinks(), EventType.METADATA,
      startedMetadataReadSinks);
    initialiseDataSink(dataSinkService.findActiveTelemetryReadSinks(), EventType.TELEMETRY,
      startedTelemetryReadSinks);
  }

  @PreDestroy
  public void stopDataSinks() {
    startedMetadataWriteSinks.entrySet().parallelStream().forEach(dataSyncEntry
      -> dataSyncEntry.getValue().disconnect());
    startedTelemetryWriteSinks.entrySet().parallelStream().forEach(dataSyncEntry
      -> dataSyncEntry.getValue().disconnect());
    startedMetadataReadSinks.entrySet().parallelStream().forEach(dataSyncEntry
      -> dataSyncEntry.getValue().disconnect());
    startedTelemetryReadSinks.entrySet().parallelStream().forEach(dataSyncEntry
      -> dataSyncEntry.getValue().disconnect());
  }

  /**
   * Distributes MQTT events to the data sinks (MQTTDataEvents are received, only when
   * the node is a cluster leader for the particular MQTT server).
   */
  @EventListener
  public void onApplicationEvent(MQTTDataEvent event) {
    LOGGER.log(Level.FINEST, "Starting distribution of MQTT event {0} on topic {1} for device {2}.",
      new Object[]{event.getId(), event.getTopic(), event.getHardwareId()});
    switch (event.getEventType()) {
      case (EventType.TELEMETRY):
        startedTelemetryWriteSinks.entrySet().parallelStream().forEach(
          dataSinkEntry -> dataSinkMessenger.processMessage(dataSinkEntry.getValue(), event));
        break;
      case (EventType.METADATA):
        startedMetadataWriteSinks.entrySet().parallelStream().forEach(
          dataSinkEntry -> dataSinkMessenger.processMessage(dataSinkEntry.getValue(), event));
        break;
      case (EventType.CONTROL_REPLY):
        break;
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
