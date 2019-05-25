package esthesis.platform.server.service;

import esthesis.platform.server.datasinks.DataSinkScanner;
import esthesis.platform.server.dto.DataSinkDTO;
import esthesis.platform.server.dto.DataSinkFactoryDTO;
import esthesis.platform.server.model.DataSink;
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

  public DataSinkService(DataSinkScanner dataSinkScanner) {
    this.dataSinkScanner = dataSinkScanner;
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
}
