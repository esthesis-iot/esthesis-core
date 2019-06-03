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

  @Override
  public DataSinkDTO save(DataSinkDTO dto) {
    return super.save(dto);
  }

  @Override
  public DataSinkDTO deleteById(long id) {
    return super.deleteById(id);
  }

  public List<DataSinkDTO> findActiveMetadataReadSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndMetadataRead(true, true));
  }

  public List<DataSinkDTO> findActiveTelemetryReadSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndTelemetryRead(true, true));
  }

  public List<DataSinkDTO> findActiveMetadataWriteSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndMetadataWrite(true, true));
  }

  public List<DataSinkDTO> findActiveTelemetryWriteSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateAndTelemetryWrite(true, true));
  }
}
