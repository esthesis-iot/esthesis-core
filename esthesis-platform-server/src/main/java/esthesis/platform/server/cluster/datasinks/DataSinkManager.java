package esthesis.platform.server.cluster.datasinks;

import com.eurodyn.qlack.common.exception.QMismatchException;
import esthesis.common.config.AppConstants.Mqtt;
import esthesis.common.datasink.DataSink;
import esthesis.common.datasink.DataSinkFactory;
import esthesis.common.datasink.MQTTDataEvent;
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
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

@Component
@Log
public class DataSinkManager {

  // JUL reference.
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

  private void initialiseDataSink(List<DataSinkDTO> dataSinks, String mqttEventType,
    Map<Long, DataSink> map) {
    dataSinks.forEach(dataSink -> {
      try {
        // Create an instance for this data sink.
        final DataSinkFactory esthesisDataSinkFactory =
          (DataSinkFactory) Class.forName(dataSink.getFactoryClass()).newInstance();
        // Set the configuration to the data sink.
        esthesisDataSinkFactory.setConfiguration(dataSink.getConfiguration());
        // Keep the instance of this data sink to a local map.
        if (mqttEventType.equals(Mqtt.EventType.METADATA)) {
          map.put(dataSink.getId(), esthesisDataSinkFactory.getMetadataSink());
          log.log(Level.FINE, "Started metadata write sink: {0}.", esthesisDataSinkFactory);
        } else if (mqttEventType.equals(Mqtt.EventType.TELEMETRY)) {
          map.put(dataSink.getId(), esthesisDataSinkFactory.getTelemetrySink());
          log.log(Level.FINE, "Started telemetry write sink: {0}.", esthesisDataSinkFactory);
        } else {
          throw new QMismatchException("Unspecified event type handler.");
        }
      } catch (Exception e) {
        log.log(Level.SEVERE, MessageFormat
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
    log.log(Level.FINEST, "Initializing active data sinks.");
    initialiseDataSink(dataSinkService.findActiveMetadataWriteSinks(), Mqtt.EventType.METADATA,
      startedMetadataWriteSinks);
    initialiseDataSink(dataSinkService.findActiveTelemetryWriteSinks(), Mqtt.EventType.TELEMETRY,
      startedTelemetryWriteSinks);
    initialiseDataSink(dataSinkService.findActiveMetadataReadSinks(), Mqtt.EventType.METADATA,
      startedMetadataReadSinks);
    initialiseDataSink(dataSinkService.findActiveTelemetryReadSinks(), Mqtt.EventType.TELEMETRY,
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
    log.log(Level.FINEST, "Starting distribution of MQTT event {0} on topic {1} from device {2}.",
      new Object[]{event.getId(), event.getTopic(), event.getHardwareId()});
    // Distinguish PING events from all others. PING events are handled in the platform database to
    // update the "last seen" indicator for a device, whereas all other events are distributed to
    // the data sinks.
    if (event.getTopic().startsWith("/" + Mqtt.EventType.TELEMETRY)) {
      startedTelemetryWriteSinks.entrySet().parallelStream().forEach(
        dataSinkEntry -> dataSinkMessenger.processMessage(dataSinkEntry.getValue(), event));
    } else if (event.getTopic().startsWith("/" + Mqtt.EventType.METADATA)) {
      startedMetadataWriteSinks.entrySet().parallelStream().forEach(
        dataSinkEntry -> dataSinkMessenger.processMessage(dataSinkEntry.getValue(), event));
    }

    log.log(Level.FINEST, "Finished distribution of MQTT event {0} on topic {1} for device {2}.",
      new Object[]{event.getId(), event.getTopic(), event.getHardwareId()});
  }

  /**
   * Handle data sink configuration changes.
   */
  @EventListener
  public void onApplicationEvent(LocalEvent event) {
    if (event.getEventType().equals(LOCAL_EVENT_TYPE.CONFIGURATION_DATASINK)) {
      log.log(Level.FINEST, "EVENT: {0}.", event);
      stopDataSinks();
      startDataSinks();
      if (!clusterInfoService.isStandalone() && !event.isClusterEvent()) {
        try {
          zookeeperClientManager.getZookeeperClient().setData()
            .forPath(Zookeeper.DATA_SINK_CONFIGURATION_EVENT_PATH,
              new ClusterEvent(CLUSTER_EVENT_TYPE.CONFIGURATION_DATA_SINK)
                .setEmitterNode(appProperties.getNodeId()).toByteArray());
        } catch (Exception e) {
          log.log(Level.SEVERE, "Could not process PlatformEvent.", e);
        }
      }
    }
  }


}
