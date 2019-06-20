package esthesis.platform.server.service;

import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import esthesis.extension.datasink.DataSink;
import esthesis.extension.datasink.dto.FieldDTO;
import esthesis.platform.server.cluster.datasinks.DataSinkManager;
import esthesis.platform.server.mapper.DevicePageMapper;
import esthesis.platform.server.model.DevicePage;
import esthesis.platform.server.repository.DeviceMetadataRepository;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
@Log
public class DevicePageService {

  private final DataSinkManager dataSinkManager;
  private final DeviceMetadataRepository deviceMetadataRepository;
  private final DevicePageMapper deviceMetadataMapper;
  private final DeviceService deviceService;

  public DevicePageService(
    DataSinkManager dataSinkManager,
    DeviceMetadataRepository deviceMetadataRepository,
    DevicePageMapper deviceMetadataMapper,
    DeviceService deviceService) {
    this.dataSinkManager = dataSinkManager;
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
    final List<FieldDTO> configuredFields = findAll().stream().filter(FieldDTO::isShown).collect(
      Collectors.toList());

    // Find measurements from which to extract fields.
    final String[] measurements = configuredFields.stream()
      .map(FieldDTO::getName)
      .map(s -> s.split("\\."))
      .map(s -> s[0])
      .distinct()
      .toArray(String[]::new);

    // Find fields.
    final String[] fields = configuredFields.stream()
      .map(FieldDTO::getName)
      .map(s -> s.split("\\."))
      .map(s -> s[1])
      .distinct()
      .toArray(String[]::new);

    final Optional<DataSink> telemetryReader = dataSinkManager.getTelemetryReader();
//    if (!telemetryReader.isPresent()) {
//      log.warning("No telemetry reader data sink available to obtain fields.");
//    } else {
//      System.out.println(
//        dataSinkManager.getTelemetryReader().get()
//          .getLast(deviceService.findById(id, false).getHardwareId(), measurements,
//            EventType.TELEMETRY, fields));
//    }

    return null;
  }

  /**
   * Finds all configured device metadata (i.e. device metadata which has been selected to be shown)
   * and complements them with new device metadata that have never been configured in the past as
   * these are discovered from the underlying data sink.
   */
  public List<FieldDTO> findAllSynthetic() {
    DataSink metadataReader = ReturnOptional.r(dataSinkManager.getTelemetryReader());

    // Get the list of all fields devices submit.
    final List<FieldDTO> fields = metadataReader.getFields();

    // Get the configuration for those fields for the UI and complement them with new fields.
    final List<FieldDTO> configuredMeasurements = findAll();
    fields.forEach(o -> {
      if (configuredMeasurements.stream().noneMatch(metadataFieldDTO
        -> o.getName().equals(metadataFieldDTO.getName()))) {
        configuredMeasurements.add(o);
      }
    });

    return configuredMeasurements;
  }
}
