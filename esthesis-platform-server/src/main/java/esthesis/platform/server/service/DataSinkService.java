package esthesis.platform.server.service;

import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONFIGURATION_DATASINK;

import esthesis.platform.server.datasinks.DataSinkScanner;
import esthesis.platform.server.dto.DataSinkDTO;
import esthesis.platform.server.dto.DataSinkFactoryDTO;
import esthesis.platform.server.events.LocalEvent;
import esthesis.platform.server.model.DataSink;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.logging.Logger;

@Service
@Validated
@Transactional
public class DataSinkService extends BaseService<DataSinkDTO, DataSink> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DataSinkService.class.getName());

  private final DataSinkScanner dataSinkScanner;
  private final ApplicationEventPublisher applicationEventPublisher;

  public DataSinkService(DataSinkScanner dataSinkScanner,
    ApplicationEventPublisher applicationEventPublisher) {
    this.dataSinkScanner = dataSinkScanner;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public List<DataSinkFactoryDTO> findAvailableDataSinkFactories() {
    return dataSinkScanner.getAvailableDataSinkFactories();
  }

  @Override
  public DataSinkDTO save(DataSinkDTO dto) {
    // Save the data sink.
    final DataSinkDTO dataSinkDTO = super.save(dto);

    // Emit an event about this configuration change.
    applicationEventPublisher.publishEvent(new LocalEvent(CONFIGURATION_DATASINK));

    return dataSinkDTO;
  }

  @Override
  public DataSinkDTO deleteById(long id) {
    final DataSinkDTO dataSinkDTO = super.deleteById(id);

    // Emit an event about this configuration change.
    applicationEventPublisher.publishEvent(new LocalEvent(CONFIGURATION_DATASINK));

    return dataSinkDTO;
  }

}
