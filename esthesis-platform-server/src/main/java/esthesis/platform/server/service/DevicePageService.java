package esthesis.platform.server.service;

import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import esthesis.extension.datasink.DataSink;
import esthesis.extension.datasink.dto.FieldDTO;
import esthesis.platform.server.cluster.datasinks.DataSinkManager;
import esthesis.platform.server.mapper.DevicePageMapper;
import esthesis.platform.server.model.DevicePage;
import esthesis.platform.server.repository.DeviceMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class DevicePageService {

  private final DataSinkManager dataSinkManager;
  private final DeviceMetadataRepository deviceMetadataRepository;
  private final DevicePageMapper deviceMetadataMapper;

  public DevicePageService(
    DataSinkManager dataSinkManager,
    DeviceMetadataRepository deviceMetadataRepository,
    DevicePageMapper deviceMetadataMapper) {
    this.dataSinkManager = dataSinkManager;
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
   * Returns the (metadata) fields stored for a particular measurement by the data sink.
   */
  public List<FieldDTO> getFieldsForMeasurement(String measurement) {
    DataSink metadataReader = ReturnOptional.r(dataSinkManager.getTelemetryReader());
    return metadataReader.getFieldsForMeasurement(measurement);
  }

  /**
   * Returns the configuration of fields as specified by the user and stored in the system's
   * database. Note that if the device has submitted new metadata fields which have never been
   * configured by the user before, this method will not include them (since such fields were never
   * stored as {@link DevicePage}. To return a list combining both, use the {@link
   * #findAllSynthetic} method instead.
   */
  public List<FieldDTO> findAll() {
    return deviceMetadataMapper.map(deviceMetadataRepository.findAll());
  }

  /**
   * Finds all configured device metadata (i.e. device metadata which has been selected to be shown)
   * and complements them with new device metadata that have never been configured in the past as
   * these are discovered from the underlying data sink.
   */
  public List<FieldDTO> findAllSynthetic(String measurement) {
    // Get the list of fields that devices submit.
    final List<FieldDTO> fieldsForMeasurement = getFieldsForMeasurement(measurement);

    // Get the configuration for those fields for the UI and complement them with new fields.
    final List<FieldDTO> configuredMeasurements = findAll();
    fieldsForMeasurement.forEach(o -> {
      if (configuredMeasurements.stream().noneMatch(metadataFieldDTO
        -> o.getName().equals(metadataFieldDTO.getName()))) {
        configuredMeasurements.add(o);
      }
    });

    return configuredMeasurements;
  }
}
