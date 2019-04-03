package esthesis.platform.server.service;

import esthesis.platform.server.dto.DataSinkDTO;
import esthesis.platform.server.dto.DataSinkFactoryDTO;
import esthesis.platform.server.mapper.DataSinkMapper;
import esthesis.platform.server.model.DataSink;
import esthesis.platform.server.repository.DataSinkRepository;
import esthesis.platform.server.datasinks.DataSinkScanner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Transactional
public class DataSinkService extends BaseService<DataSinkDTO, DataSink> {

  private final DataSinkRepository dataSinkRepository;
  private final DataSinkMapper dataSinkMapper;
  private final DataSinkScanner sinkScanner;

  public DataSinkService(DataSinkRepository dataSinkRepository,
      DataSinkMapper dataSinkMapper, DataSinkScanner sinkScanner) {
    this.dataSinkRepository = dataSinkRepository;
    this.dataSinkMapper = dataSinkMapper;
    this.sinkScanner = sinkScanner;
  }

  public List<DataSinkDTO> findActiveMetadataSinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateIsTrueAndMetadataIsTrue());
  }

  public List<DataSinkDTO> findActiveTelemetrySinks() {
    return dataSinkMapper.map(dataSinkRepository.findAllByStateIsTrueAndMetadataIsTrue());
  }

  public List<DataSinkFactoryDTO> findAvailableDataSinkFactories() {
    return sinkScanner.getAvailableDataSinkFactories();
  }

  @Override
  public DataSinkDTO save(DataSinkDTO dto) {
    return super.save(dto);
  }
}
