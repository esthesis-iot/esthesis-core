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
  private final DeviceService deviceService;

  public DevicePageService(
//    DataSinkManager dataSinkManager,
    DeviceMetadataRepository deviceMetadataRepository,
    DevicePageMapper deviceMetadataMapper,
    DeviceService deviceService) {
//    this.dataSinkManager = dataSinkManager;
    this.deviceMetadataRepository = deviceMetadataRepository;
    this.deviceMetadataMapper = deviceMetadataMapper;
    this.deviceService = deviceService;
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
   * database. Note that if the device has submitted new metadata fields which have never been
   * configured by the user before, this method will not include them (since such fields were never
   * stored as {@link DevicePage}. To return a list combining both, use the {@link
   * #findAllSynthetic} method instead.
   */
  public List<FieldDTO> findAll() {
    return deviceMetadataMapper.map(deviceMetadataRepository.findAll());
  }

  /**
   * Returns all fields configured to be shown on the device page with each field populated with its
   * latest value.
   *
   * @param id The ID of the device for which to fetch the values of the fields.
   */
  public List<FieldDTO> findWithLatestValues(long id) {

    // Get all fields that the user has configure to see in the device page.
    final List<FieldDTO> configuredFields = deviceMetadataMapper
      .map(deviceMetadataRepository.findAllByShownIsTrue());

    //TODO each field is sequentially queried, the algorithm can be optimised to query for all fields at once.
//    final Optional<DataSink> telemetryReader = dataSinkManager.getTelemetryReader();
//    if (!telemetryReader.isPresent()) {
//      log.warning("No telemetry reader data sink available to obtain fields.");
//      return new ArrayList<>();
//    } else {
//      String hardwareId = deviceService.findById(id, false).getHardwareId();
//      return configuredFields.stream().map(fieldDTO -> {
//        String measurement = fieldDTO.getName().split("\\.")[0];
//        String field = fieldDTO.getName().split("\\.")[1];
//        final DataSinkQueryResult last = telemetryReader.get()
//          .getLast(hardwareId, measurement, EventType.TELEMETRY, new String[]{field});
//        if (last != null && CollectionUtils.isNotEmpty(last.getValues()) && CollectionUtils
//          .isNotEmpty(last.getValues().get(0))) {
//          fieldDTO.setValue(last.getValues().get(0).get(1));
//        }
//        return fieldDTO;
//      }).collect(Collectors.toList());
//    }
    return null;
  }

  /**
   * Finds all configured device metadata (i.e. device metadata which has been selected to be shown)
   * and complements them with new device metadata that have never been configured in the past as
   * these are discovered from the underlying data sink.
   */
  public List<FieldDTO> findAllSynthetic() {
//    final Optional<DataSink> telemetryReader = dataSinkManager.getTelemetryReader();
//    if (!telemetryReader.isPresent()) {
//      return new ArrayList<>();
//    } else {
//      // Get the list of all fields devices submit.
//      final List<FieldDTO> fields = telemetryReader.get().getFields();
//
//      // Get the configuration for those fields for the UI and complement them with new fields.
//      final List<FieldDTO> configuredMeasurements = findAll();
//      fields.forEach(o -> {
//        if (configuredMeasurements.stream().noneMatch(metadataFieldDTO
//          -> o.getName().equals(metadataFieldDTO.getName()))) {
//          configuredMeasurements.add(o);
//        }
//      });
//
//      return configuredMeasurements;
//    }
    return null;
  }
}
