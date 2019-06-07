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

import java.util.Map.Entry;
import java.util.Optional;

@Service
@Validated
@Transactional
public class DTService {

  private final DataSinkManager dataSinkManager;

  public DTService(DataSinkManager dataSinkManager) {
    this.dataSinkManager = dataSinkManager;
  }

  private DataSink getDataSink(String eventType) {
    Optional<Entry<Long, DataSink>> dataSinkEntry = Optional.empty();

    switch (eventType) {
      case EventType.METADATA:
        dataSinkEntry = dataSinkManager.getStartedMetadataReadSinks()
          .entrySet().stream().findAny();
        break;
      case EventType.TELEMETRY:
        dataSinkEntry = dataSinkManager.getStartedTelemetryReadSinks()
          .entrySet().stream().findAny();
        break;
    }

    if (dataSinkEntry.isPresent()) {
      return dataSinkEntry.get().getValue();
    } else {
      throw new QDoesNotExistException("Could not find a matching data sink.");
    }
  }


  public DataSinkMeasurement getFirst(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, String fields) {
    return getDataSink(type).getFirst(hardwareId, measurement, type, fields);
  }

  public DataSinkMeasurement getLast(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, String fields) {
    return getDataSink(type).getLast(hardwareId, measurement, type, fields);
  }

  public DataSinkMeasurement get(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, Integer page, Integer pageSize,
    String fields) {
    return getDataSink(type).get(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0L),
      ObjectUtils.defaultIfNull(to, 0L), ObjectUtils.defaultIfNull(page, 0),
      ObjectUtils.defaultIfNull(pageSize, 0), fields);
  }

  public DataSinkMeasurement count(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, String fields) {
    return getDataSink(type)
      .count(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), fields);
  }

  public DataSinkMeasurement max(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, @NotNull String fields) {
    return getDataSink(type)
      .max(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), fields);
  }

  public DataSinkMeasurement min(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, @NotNull String fields) {
    return getDataSink(type)
      .min(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), fields);
  }

  public DataSinkMeasurement sum(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, @NotNull String fields) {
    return getDataSink(type)
      .sum(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), fields);
  }

  public DataSinkMeasurement average(@NotNull String hardwareId, @NotNull String type,
    @NotNull String measurement, Long from, Long to, @NotNull String fields) {
    return getDataSink(type)
      .average(hardwareId, measurement, type, ObjectUtils.defaultIfNull(from, 0l),
        ObjectUtils.defaultIfNull(to, 0L), fields);
  }
}
