package esthesis.platform.server.sinks;

import esthesis.extension.platform.sink.EsthesisDataSinkFactory;
import esthesis.extension.platform.sink.EsthesisMetadataSink;
import esthesis.extension.platform.sink.EsthesisTelemetrySink;
import esthesis.platform.server.service.DataSinkService;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SinkScanner {
  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(SinkScanner.class.getName());
  private static final String DATA_SINK_FACTORY_PACKAGE = "esthesis.extension.platform.sink.EsthesisDataSinkFactory";
  // The list of available and active metadata data sinks.
  private final Map<String, String> availableMetadataSinks = new HashMap<>();
  private final Map<String, EsthesisMetadataSink> activeMetadataSinks = new HashMap<>();
  // The list of available telemetry data sinks.
  private final Map<String, String> availableTelemetrySinks = new HashMap<>();
  private final Map<String, EsthesisTelemetrySink> activeTelemetrySinks = new HashMap<>();
  private final DataSinkService dataSinkService;

  public SinkScanner(DataSinkService dataSinkService) {
    this.dataSinkService = dataSinkService;
  }

  @PostConstruct
  public void start() {
    // Find data sink classes.
    try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
      ClassInfoList factoryClasses = scanResult.getClassesImplementing(DATA_SINK_FACTORY_PACKAGE);
      factoryClasses.forEach(classInfo -> {
        try {
          final EsthesisDataSinkFactory esthesisDataSinkFactory = classInfo.loadClass(EsthesisDataSinkFactory.class)
              .newInstance();
          if (esthesisDataSinkFactory.getMetadataSink() != null) {
            availableMetadataSinks.put(classInfo.getName(), esthesisDataSinkFactory.getFriendlyName());
          }
          if (esthesisDataSinkFactory.getTelemetrySink() != null) {
            availableTelemetrySinks.put(classInfo.getName(), esthesisDataSinkFactory.getFriendlyName());
          }
          LOGGER.log(Level.FINE, "Found data sink implementation ''{0}'' [class: {1}].",
              new Object[]{esthesisDataSinkFactory.getFriendlyName(), classInfo.getName()});
        } catch (InstantiationException | IllegalAccessException e) {
          LOGGER.log(Level.SEVERE, "Could not load data sink implementation {0}.", classInfo.getName());
        }
      });
    }

    // Find active data sinks.
    updateActiveSinks();
  }

  public Map<String, String> getAvailableMetadataSinks() {
    return availableMetadataSinks;
  }

  public Map<String, String> getAvailableTelemetrySinks() {
    return availableTelemetrySinks;
  }

  public void updateActiveSinks() {
//    activeMetadataSinks.clear();
//    dataSinkService.findAllByMetadataSinkActiveIsTrueAnd().forEach(dataSink -> {
//      try {
//        final EsthesisDataSinkFactory esthesisDataSinkFactory = (EsthesisDataSinkFactory) Class
//            .forName(dataSink.getFactoryClass()).newInstance();
//        esthesisDataSinkFactory.setConfiguration(dataSink.getConfiguration());
//
//        final EsthesisMetadataSink esthesisMetadataSink =
//
//        activeMetadataSinks.put(dataSink.getFactoryClass() + "_" + dataSink.getId(),
//            );
//      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
//        LOGGER.log(Level.SEVERE, "Could not instantiate data sink {0}.", dataSink.getFactoryClass());
//      }
//    });
  }

  @PreDestroy
  public void stop() {

  }

}
