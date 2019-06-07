package esthesis.platform.server.service;

import esthesis.platform.server.datasinks.DataSinkScanner;
import esthesis.platform.server.dto.DataSinkDTO;
import esthesis.platform.server.dto.DataSinkFactoryDTO;
import esthesis.platform.server.mapper.DataSinkMapper;
import esthesis.platform.server.model.DataSink;
import esthesis.platform.server.repository.DataSinkRepository;
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
  private final DataSinkMapper dataSinkMapper;
  private final DataSinkRepository dataSinkRepository;

  public DataSinkService(DataSinkScanner dataSinkScanner,
    DataSinkMapper dataSinkMapper,
    DataSinkRepository dataSinkRepository) {
    this.dataSinkScanner = dataSinkScanner;
    this.dataSinkMapper = dataSinkMapper;
    this.dataSinkRepository = dataSinkRepository;
  }

  public List<DataSinkFactoryDTO> findAvailableDataSinkFactories() {
    return dataSinkScanner.getAvailableDataSinkFactories();
  }

  /**
   * Returns data sinks which are declared as active and can read metadata data. Note that this
   * method does not guarantee that the returned data sinks are also initialised by the platform.
   * Initialised data sinks can be queried from
   * {@link esthesis.platform.server.cluster.datasinks.DataSinkManager}.
   */
  public List<DataSinkDTO> findActiveMetadataReadSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndMetadataRead(true, true));
  }

  /**
   * Returns data sinks which are declared as active and can read telemetry data. Note that this
   * method does not guarantee that the returned data sinks are also initialised by the platform.
   * Initialised data sinks can be queried from
   * {@link esthesis.platform.server.cluster.datasinks.DataSinkManager}.
   */
  public List<DataSinkDTO> findActiveTelemetryReadSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndTelemetryRead(true, true));
  }

  /**
   * Returns data sinks which are declared as active and can write metadata data. Note that this
   * method does not guarantee that the returned data sinks are also initialised by the platform.
   * Initialised data sinks can be queried from
   * {@link esthesis.platform.server.cluster.datasinks.DataSinkManager}.
   */
  public List<DataSinkDTO> findActiveMetadataWriteSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndMetadataWrite(true, true));
  }

  /**
   * Returns data sinks which are declared as active and can write telemetry data. Note that this
   * method does not guarantee that the returned data sinks are also initialised by the platform.
   * Initialised data sinks can be queried from
   * {@link esthesis.platform.server.cluster.datasinks.DataSinkManager}.
   */
  public List<DataSinkDTO> findActiveTelemetryWriteSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndTelemetryWrite(true, true));
  }
}
