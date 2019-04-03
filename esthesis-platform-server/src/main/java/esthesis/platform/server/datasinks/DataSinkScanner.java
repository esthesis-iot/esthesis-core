package esthesis.platform.server.datasinks;

import esthesis.extension.platform.sink.EsthesisDataSinkFactory;
import esthesis.platform.server.dto.DataSinkFactoryDTO;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import javax.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class DataSinkScanner {
  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DataSinkScanner.class.getName());
  private static final String DATA_SINK_FACTORY_PACKAGE = "esthesis.extension.platform.sink.EsthesisDataSinkFactory";
  // List of available data sink factories.
  private final List<DataSinkFactoryDTO> availableDataSinkFactories = new ArrayList<>();

  @Async
  @PostConstruct
  public void start() {
    // Find data sink classes.
    try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
      ClassInfoList factoryClasses = scanResult.getClassesImplementing(DATA_SINK_FACTORY_PACKAGE);
      factoryClasses.forEach(classInfo -> {
        try {
          final EsthesisDataSinkFactory esthesisDataSinkFactory = classInfo.loadClass(EsthesisDataSinkFactory.class)
              .newInstance();
          availableDataSinkFactories.add(DataSinkFactoryDTO.builder()
              .factoryClass(classInfo.getName())
              .friendlyName(esthesisDataSinkFactory.getFriendlyName())
              .supportsMetadata(esthesisDataSinkFactory.supportsMetadata())
              .supportsTelemetry(esthesisDataSinkFactory.supportsTelemetry())
              .configurationTemplate(esthesisDataSinkFactory.getConfigurationTemplate())
              .build());
          LOGGER.log(Level.FINE, "Found data sink factory ''{0}'' [class: {1}].",
              new Object[]{esthesisDataSinkFactory.getFriendlyName(), classInfo.getName()});
        } catch (InstantiationException | IllegalAccessException e) {
          LOGGER.log(Level.SEVERE, "Could not load data sink implementation {0}.", classInfo.getName());
        }
      });
    }
  }

  public List<DataSinkFactoryDTO> getAvailableDataSinkFactories() {
    return availableDataSinkFactories;
  }
}
