package esthesis.platform.server.service;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import esthesis.common.config.AppConstants.Mqtt;
import esthesis.common.datasink.DataSink;
import esthesis.common.datasink.dto.DataSinkQueryResult;
import esthesis.platform.server.cluster.datasinks.DataSinkManager;
import esthesis.platform.server.model.Device;
import esthesis.platform.server.repository.DeviceRepository;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class DTService {

  private final DataSinkManager dataSinkManager;
  private final DeviceRepository deviceRepository;

  public DTService(DataSinkManager dataSinkManager,
    DeviceRepository deviceRepository) {
    this.dataSinkManager = dataSinkManager;
    this.deviceRepository = deviceRepository;
  }

  private DataSink getDataSinkReader(String mqttEventType) {
    Optional<DataSink> dataSinkEntry = Optional.empty();

    switch (mqttEventType) {
      case Mqtt.EventType.METADATA:
        dataSinkEntry = dataSinkManager.getMetadataReader();
        break;
      case Mqtt.EventType.TELEMETRY:
        dataSinkEntry = dataSinkManager.getTelemetryReader();
        break;
    }

    if (!dataSinkEntry.isPresent()) {
      throw new QDoesNotExistException("Could not find a matching data sink.");
    }

    return dataSinkEntry.get();
  }

  private DataSink getDataSinkWriter(String mqttEventType) {
    Optional<DataSink> dataSinkEntry = Optional.empty();

    switch (mqttEventType) {
      case Mqtt.EventType.METADATA:
        dataSinkEntry = dataSinkManager.getMetadataWriter();
        break;
      case Mqtt.EventType.TELEMETRY:
        dataSinkEntry = dataSinkManager.getTelemetryWriter();
        break;
    }

    if (!dataSinkEntry.isPresent()) {
      throw new QDoesNotExistException("Could not find a matching data sink.");
    }

    return dataSinkEntry.get();
  }

  public DataSinkQueryResult getFirst(@NotNull String hardwareId, @NotNull String mqttEventType,
    @NotNull String measurement, String[] fields) {
    return getDataSinkReader(mqttEventType)
      .getFirst(hardwareId, measurement, mqttEventType,
        fields);
  }

  public DataSinkQueryResult getLast(@NotNull String hardwareId, @NotNull String mqttEventType,
    @NotNull String measurements, String[] fields) {
    return getDataSinkReader(mqttEventType)
      .getLast(hardwareId, measurements, mqttEventType, fields);
  }

  public DataSinkQueryResult get(@NotNull String hardwareId, @NotNull String mqttEventType,
    @NotNull String measurements, Long from, Long to, Integer page, Integer pageSize,
    String[] fields) {
    return getDataSinkReader(mqttEventType)
      .get(hardwareId, measurements, mqttEventType, ObjectUtils.defaultIfNull(from, 0L),
        ObjectUtils.defaultIfNull(to, 0L), ObjectUtils.defaultIfNull(page, 0),
        ObjectUtils.defaultIfNull(pageSize, 0), fields);
  }

  public DataSinkQueryResult count(@NotNull String hardwareId, @NotNull String mqttEventType,
    @NotNull String measurements, Long from, Long to, String field) {
    return getDataSinkReader(mqttEventType)
      .count(hardwareId, measurements, mqttEventType, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), field);
  }

  public DataSinkQueryResult max(@NotNull String hardwareId, @NotNull String mqttEventType,
    @NotNull String measurements, Long from, Long to, @NotNull String field) {
    return getDataSinkReader(mqttEventType)
      .max(hardwareId, measurements, mqttEventType, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), field);
  }

  public DataSinkQueryResult min(@NotNull String hardwareId, @NotNull String mqttEventType,
    @NotNull String measurements, Long from, Long to, @NotNull String field) {
    return getDataSinkReader(mqttEventType)
      .min(hardwareId, measurements, mqttEventType, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), field);
  }

  public DataSinkQueryResult sum(@NotNull String hardwareId, @NotNull String mqttEventType,
    @NotNull String measurements, Long from, Long to, @NotNull String field) {
    return getDataSinkReader(mqttEventType)
      .sum(hardwareId, measurements, mqttEventType, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), field);
  }

  public DataSinkQueryResult average(@NotNull String hardwareId, @NotNull String mqttEventType,
    @NotNull String measurements, Long from, Long to, @NotNull String field) {
    return getDataSinkReader(mqttEventType)
      .average(hardwareId, measurements, mqttEventType, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), field);
  }

  /**
   * Returns the list of devices registered
   */
  public List<String> getDevicesRegisteredAfter(Instant date) {
    return deviceRepository.findAllByCreatedOnAfter(date).stream()
      .map(Device::getHardwareId)
      .collect(Collectors.toList());
  }
}
