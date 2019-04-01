package esthesis.platform.server.service;

import esthesis.extension.config.AppConstants;
import esthesis.platform.server.dto.DataSinkDTO;
import esthesis.platform.server.model.DataSink;
import esthesis.platform.server.repository.DataSinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Transactional
public class DataSinkService extends BaseService<DataSinkDTO, DataSink> {

  private final DataSinkRepository dataSinkRepository;

  public DataSinkService(DataSinkRepository dataSinkRepository) {
    this.dataSinkRepository = dataSinkRepository;
  }

  public List<DataSink> findActiveMetadataSinks() {
    return dataSinkRepository.findAllByStateIsTrueAndSinkType(AppConstants.DataSink.TYPE_METADATA);
  }

  public List<DataSink> findActiveTelemetrySinks() {
    return dataSinkRepository.findAllByStateIsTrueAndSinkType(AppConstants.DataSink.TYPE_TELEMETRY);
  }

}
