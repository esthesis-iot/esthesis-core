package esthesis.common.datasink;

import esthesis.common.datasink.dto.DataSinkQueryResult;
import esthesis.common.datasink.dto.FieldDTO;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.NonNull;

import java.util.List;

/**
 * Common functionality for all types of data sinks.
 */
public interface DataSink {

  /**
   * Disconnects the data sink from the underlying persistence implementation.
   */
  void disconnect();

  /**
   * Returns the pressure of the data sink handler. Pressure is a value between 0 and 1, indicating
   * how many messages are queued by the data sink waiting for persistence. Esthesis platform
   * queries the pressure value of each data sink and when pressure is >90% (i.e. 0.9) it stops
   * sending additional messages until pressure drops to <90%.
   */
  float getPressure();

  /**
   * A friendly name to be displayed on the UI of the platform for this data sink handler.
   */
  String getFriendlyName();

  /**
   * Get the latest value of a measurement.
   *
   * @param measurement
   * @param fields A comma-separated list of fields to return.
   */
  DataSinkQueryResult getLast(@NotNull String hardwareId, @NotEmpty String measurement,
      @NotNull String eventType, @NotEmpty String[] fields);

  /**
   * Get the first (time-wised) valude of a measurement.
   *
   * @param fields A comma-separated list of fields to return.
   */
  DataSinkQueryResult getFirst(@NotNull String hardwareId, @NotEmpty String measurement,
      @NotNull String eventType, @NotNull String[] fields);

  /**
   * Gets a paginated result of the values of a mesurement for a time period.
   *
   * @param fromDate An EPOCH in milliseconds.
   * @param toDate An EPOCH in milliseconds.
   * @param fields A comma-separated list of fields to return.
   */
  DataSinkQueryResult get(@NonNull String hardwareId, @NotEmpty String measurement,
      @NotNull String eventType, long fromDate, long toDate, int page, int pageSize,
      @NotEmpty String[] fields);

  /**
   * Gets the total number of measurements for a time period.
   *
   * @param fromDate An EPOCH in milliseconds.
   * @param toDate An EPOCH in milliseconds.
   */
  DataSinkQueryResult count(@NotNull String hardwareId, @NotEmpty String measurement,
      @NotNull String eventType, long fromDate, long toDate, String field);

  DataSinkQueryResult max(@NotNull String hardwareId, @NotEmpty String measurement,
      @NotNull String eventType, long fromDate, long toDate, @NotEmpty String field);

  DataSinkQueryResult min(@NotNull String hardwareId, @NotEmpty String measurement,
      @NotNull String eventType, long fromDate, long toDate, @NotEmpty String field);

  DataSinkQueryResult sum(@NotNull String hardwareId, @NotEmpty String measurement,
      @NotNull String eventType, long fromDate, long toDate, @NotEmpty String field);

  DataSinkQueryResult average(@NotNull String hardwareId, @NotEmpty String measurement,
      @NotNull String eventType, long fromDate, long toDate, @NotEmpty String field);

  /**
   * Processes an data event received from MQTT.
   *
   * @param event An encapsulation of the MQTT message that was received.
   */
  void processEvent(MQTTDataEvent event);

  /**
   * Returns the list of fields (i.e. values) available for a measurement.
   *
   * @param measurement The measurement to return the values of.
   */
  List<FieldDTO> getFieldsForMeasurement(String measurement);

  /**
   * Returns the list of fields (i.e. values) available for all measurements.
   */
  List<FieldDTO> getFields();
}
