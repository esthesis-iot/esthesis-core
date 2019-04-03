package esthesis.platform.server.datasinks;

import esthesis.extension.platform.event.MQTTDataEvent;
import esthesis.extension.platform.event.MQTTMetadataEvent;
import esthesis.extension.platform.event.MQTTTelemetryEvent;
import esthesis.extension.platform.sink.EsthesisDataSinkFactory;
import esthesis.extension.platform.sink.EsthesisMetadataSink;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;
import esthesis.platform.server.service.DataSinkService;
import esthesis.platform.server.service.ZookeeperService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DataSinkDistributor implements ApplicationListener<MQTTDataEvent> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DataSinkDistributor.class.getName());
  private final DataSinkMessenger sinkMessenger;
  private final DataSinkService dataSinkService;
  // List of available and active metadata data sinks.
  private final Map<Long, EsthesisMetadataSink> activeMetadataSinks = new HashMap<>();
  // List of available telemetry data sinks.
  private final Map<Long, EsthesisTelemetrySink> activeTelemetrySinks = new HashMap<>();
  private final ZookeeperService zookeeperService;

  public DataSinkDistributor(DataSinkMessenger sinkMessenger,
      DataSinkService dataSinkService, ZookeeperService zookeeperService) {
    this.sinkMessenger = sinkMessenger;
    this.dataSinkService = dataSinkService;
    this.zookeeperService = zookeeperService;
  }

  @PostConstruct
  public void findActiveSinks() {
    LOGGER.log(Level.FINEST, "Initializing active data sinks.");
    // Get all registered active metadata sinks.
    dataSinkService.findActiveMetadataSinks().forEach(dataSink -> {
      try {
        // Create an instance for this data sink.
        final EsthesisDataSinkFactory esthesisDataSinkFactory =
            (EsthesisDataSinkFactory) Class.forName(dataSink.getFactoryClass()).newInstance();
        // Set the configuration to the data sink.
        esthesisDataSinkFactory.setConfiguration(dataSink.getConfiguration());
        // Keep the instance of this data sink to a local map.
        activeMetadataSinks.put(dataSink.getId(), esthesisDataSinkFactory.getMetadataSink());
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        LOGGER.log(Level.SEVERE, "Could not instantiate metadata data sink {0}.", dataSink.getFactoryClass());
      }
    });

    // Get all registered active telemetry sinks.
    dataSinkService.findActiveTelemetrySinks().forEach(dataSink -> {
      try {
        // Create an instance for this data sink.
        final EsthesisDataSinkFactory esthesisDataSinkFactory =
            (EsthesisDataSinkFactory) Class.forName(dataSink.getFactoryClass()).newInstance();
        // Set the configuration to the data sink.
        esthesisDataSinkFactory.setConfiguration(dataSink.getConfiguration());
        // Keep the instance of this data sink to a local map.
        activeTelemetrySinks.put(dataSink.getId(), esthesisDataSinkFactory.getTelemetrySink());
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        LOGGER.log(Level.SEVERE, "Could not instantiate telemetry data sink {0}.", dataSink.getFactoryClass());
      }
    });
  }

  @Override
  public void onApplicationEvent(MQTTDataEvent event) {
    LOGGER.log(Level.FINEST, "Distributor got event: {0}.", event);
    // Pass events to data sinks only if this node is a cluster leader (or in standalone mode).
    if (!zookeeperService.isCluster() || zookeeperService.isLeader()) {
      if (event instanceof MQTTMetadataEvent) {
        activeMetadataSinks.entrySet().parallelStream().forEach(
            sink -> sinkMessenger.metadataMessage(sink.getValue(), event));
      } else if (event instanceof MQTTTelemetryEvent) {
        activeTelemetrySinks.entrySet().parallelStream().forEach(
            sink -> sinkMessenger.telemetryMessage(sink.getValue(), event));
      }
    }
    LOGGER.log(Level.FINEST, "Finished distributor for event: {0}.", event);
  }

  @PreDestroy
  public void stop() {

  }
}
