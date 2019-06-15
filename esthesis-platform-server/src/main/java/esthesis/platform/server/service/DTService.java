package esthesis.platform.server.service;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import esthesis.extension.datasink.DataSink;
import esthesis.extension.datasink.config.AppConstants.Mqtt.EventType;
import esthesis.extension.datasink.dto.DataSinkMeasurement;
import esthesis.platform.server.cluster.datasinks.DataSinkManager;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
@Transactional
public class DTService {

  private final DataSinkManager dataSinkManager;

  public DTService(DataSinkManager dataSinkManager) {
    this.dataSinkManager = dataSinkManager;
  }

  private DataSink getDataSinkReader(String eventType) {
    Optional<DataSink> dataSinkEntry = Optional.empty();

    switch (eventType) {
      case EventType.METADATA:
        dataSinkEntry = dataSinkManager.getMetadataReader();
        break;
      case EventType.TELEMETRY:
        dataSinkEntry = dataSinkManager.getTelemetryReader();
        break;
    }

    if (!dataSinkEntry.isPresent()) {
      throw new QDoesNotExistException("Could not find a matching data sink.");
    }

    return dataSinkEntry.get();
  }

  private DataSink getDataSinkWriter(String eventType) {
    Optional<DataSink> dataSinkEntry = Optional.empty();

    switch (eventType) {
      case EventType.METADATA:
        dataSinkEntry = dataSinkManager.getMetadataWriter();
        break;
      case EventType.TELEMETRY:
        dataSinkEntry = dataSinkManager.getTelemetryWriter();
        break;
    }

    if (!dataSinkEntry.isPresent()) {
      throw new QDoesNotExistException("Could not find a matching data sink.");
    }

    return dataSinkEntry.get();
  }

  public DataSinkMeasurement getFirst(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, String fields) {
    return getDataSinkReader(type).getFirst(hardwareId, measurement, type, fields);
  }

  public DataSinkMeasurement getLast(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, String fields) {
    return getDataSinkReader(type).getLast(hardwareId, measurement, type, fields);
  }

  public DataSinkMeasurement get(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, Integer page, Integer pageSize,
    String fields) {
    return getDataSinkReader(type).get(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0L),
      ObjectUtils.defaultIfNull(to, 0L), ObjectUtils.defaultIfNull(page, 0),
      ObjectUtils.defaultIfNull(pageSize, 0), fields);
  }

  public DataSinkMeasurement count(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, String fields) {
    return getDataSinkReader(type)
      .count(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), fields);
  }

  public DataSinkMeasurement max(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, @NotNull String fields) {
    return getDataSinkReader(type)
      .max(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), fields);
  }

  public DataSinkMeasurement min(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, @NotNull String fields) {
    return getDataSinkReader(type)
      .min(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), fields);
  }

  public DataSinkMeasurement sum(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, @NotNull String fields) {
    return getDataSinkReader(type)
      .sum(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), fields);
  }

  public DataSinkMeasurement average(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, @NotNull String fields) {
    return getDataSinkReader(type)
      .average(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), fields);
  }
}
