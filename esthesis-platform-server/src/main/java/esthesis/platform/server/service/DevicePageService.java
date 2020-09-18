package esthesis.platform.server.service;


import esthesis.common.datasink.dto.FieldDTO;
import esthesis.platform.server.mapper.DevicePageMapper;
import esthesis.platform.server.model.DevicePage;
import esthesis.platform.server.repository.DeviceMetadataRepository;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
@Log
public class DevicePageService {

//  private final DataSinkManager dataSinkManager;
  private final DeviceMetadataRepository deviceMetadataRepository;
  private final DevicePageMapper deviceMetadataMapper;

  public DevicePageService(
    DeviceMetadataRepository deviceMetadataRepository,
    DevicePageMapper deviceMetadataMapper) {
    this.deviceMetadataRepository = deviceMetadataRepository;
    this.deviceMetadataMapper = deviceMetadataMapper;
  }

  public void save(List<FieldDTO> fields) {
    deviceMetadataRepository.saveAll(
      fields.stream().map(field -> {
        DevicePage deviceMetadata = deviceMetadataRepository.findByName(field.getName());
        if (deviceMetadata == null) {
          deviceMetadata = new DevicePage();
        }
        return deviceMetadataMapper.map(field, deviceMetadata);
      }).collect(Collectors.toList()));
  }

  /**
   * Returns the configuration of fields as specified by the user and stored in the system's
   * database.
   */
  public List<FieldDTO> findAll() {
    return deviceMetadataMapper.map(deviceMetadataRepository.findAll());
  }

}
