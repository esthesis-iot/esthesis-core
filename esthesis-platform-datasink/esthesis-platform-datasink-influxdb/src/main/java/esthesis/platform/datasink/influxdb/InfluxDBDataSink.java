package esthesis.platform.datasink.influxdb;

import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.asc;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.desc;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.eq;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.gte;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.lte;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.select;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.extension.datasink.DataSink;
import esthesis.extension.datasink.MQTTDataEvent;
import esthesis.extension.datasink.config.AppConstants.Mqtt;
import esthesis.extension.datasink.dto.DataSinkMeasurement;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Series;
import org.influxdb.querybuilder.Ordering;
import org.influxdb.querybuilder.SelectionQueryImpl;
import org.influxdb.querybuilder.WhereQueryImpl;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InfluxDBDataSink implements DataSink {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(InfluxDBDataSink.class.getName());
  private InfluxDB influxDB;
  private boolean initialized;
  private InfluxDBConfiguration influxDBConfiguration;
  private String sinkName;
  private String eventType;
  // The name of the tag to indicate the type of the measurement.
  public static final String TAG_TYPE_NAME = "type";
  // The name of the tag to indicate the hardware ID for which a measurement was recorded.
  public static final String TAG_HARDWARE_ID_NAME = "hardwareId";
  // The default name of the field when no specific name is provided for a measurement.
  public static final String NO_NAME_VALUE = "value";
  // The default InfluxDB name used for time column.
  public static final String INFLUXDB_TIME_NAME = "time";

  private static ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(
    Include.NON_EMPTY);
  private static AtomicInteger eventsQueued = new AtomicInteger(0);

  InfluxDBDataSink(String configuration, String sinkName, String eventType) {
    LOGGER.log(Level.FINE, "Instantiating {0}.", sinkName);
    this.sinkName = sinkName;
    this.eventType = eventType;

    // Parse configuration.
    Representer representer = new Representer();
    representer.getPropertyUtils()
      .setSkipMissingProperties(true);
    influxDBConfiguration = new Yaml(new Constructor(InfluxDBConfiguration.class), representer)
      .load(configuration);

    // Connect to database.
    influxDB = connect(influxDBConfiguration);

    initialized = true;
    LOGGER.log(Level.FINE, "Instantiated {0}.", sinkName);
  }

  private InfluxDB connect(InfluxDBConfiguration influxDBConfiguration) {
    return InfluxDBFactory
      .connect(
        influxDBConfiguration.getDatabaseUrl(),
        influxDBConfiguration.getUsername(),
        influxDBConfiguration.getPassword())
      .setDatabase(influxDBConfiguration.getDatabaseName())
      .enableBatch(BatchOptions.DEFAULTS.exceptionHandler(
        (failedPoints, throwable) -> {
          LOGGER.log(Level.SEVERE, "Could not persist data to InfluxDB.", throwable);
        })
      );
  }

  private Point preparePoint(String hardwareId, byte[] mqttPayload, String eventType)
  throws IOException {
    // Read the payload of the MQTT message.
    final JsonNode jsonNode = mapper.readTree(mqttPayload);

    // Prepare an InfluxDB Point with the name of the measurement.
    String metricName = jsonNode.get(Mqtt.MqttPayload.METRIC_KEYNAME).asText();
    final Builder pointBuilder = org.influxdb.dto.Point.measurement(metricName);

    // Add a tag to the Point for the type of the message.
    pointBuilder.tag(TAG_TYPE_NAME, eventType);

    // Add a tag to the Point for the device ID.
    pointBuilder.tag(TAG_HARDWARE_ID_NAME, hardwareId);

    // If a timestamp is provided use the provided timestamp, otherwise create a timestamp.
    if (jsonNode.get(Mqtt.MqttPayload.TIMESTAMP_KEYNAME) != null) {
      pointBuilder
        .time(jsonNode.get(Mqtt.MqttPayload.TIMESTAMP_KEYNAME).asLong(), TimeUnit.MILLISECONDS);
    } else {
      pointBuilder.time(Instant.now().toEpochMilli(), TimeUnit.MILLISECONDS);
    }

    // Find the values of the payload.
    final JsonNode values = jsonNode.get(Mqtt.MqttPayload.VALUES_KEYNAME);
    if (values.isObject()) {
      values.fields().forEachRemaining(node -> {
        final JsonNode value = node.getValue();
        if (value.isLong()) {
          pointBuilder.addField(node.getKey(), value.asLong());
        } else if (value.isDouble() || value.isFloat()) {
          pointBuilder.addField(node.getKey(), value.asDouble());
        } else if (value.isTextual()) {
          pointBuilder.addField(node.getKey(), value.asText());
        } else if (value.isBoolean()) {
          pointBuilder.addField(node.getKey(), value.asBoolean());
        } else if (value.isInt() || value.isShort()) {
          pointBuilder.addField(node.getKey(), value.asInt());
        } else if (value.isBigInteger()) {
          pointBuilder.addField(node.getKey(), BigInteger.valueOf(value.asLong()));
        } else if (value.isBigDecimal()) {
          pointBuilder.addField(node.getKey(), BigDecimal.valueOf(value.asDouble()));
        }
      });
    } else {
      if (values.isLong()) {
        pointBuilder.addField(NO_NAME_VALUE, values.asLong());
      } else if (values.isDouble() || values.isFloat()) {
        pointBuilder.addField(NO_NAME_VALUE, values.asDouble());
      } else if (values.isTextual()) {
        pointBuilder.addField(NO_NAME_VALUE, values.asText());
      } else if (values.isBoolean()) {
        pointBuilder.addField(NO_NAME_VALUE, values.asBoolean());
      } else if (values.isInt() || values.isShort()) {
        pointBuilder.addField(NO_NAME_VALUE, values.asInt());
      } else if (values.isBigInteger()) {
        pointBuilder.addField(NO_NAME_VALUE, BigInteger.valueOf(values.asLong()));
      } else if (values.isBigDecimal()) {
        pointBuilder.addField(NO_NAME_VALUE, BigDecimal.valueOf(values.asDouble()));
      }
    }

    return pointBuilder.build();
  }

  public void processEvent(MQTTDataEvent event) {
    eventsQueued.incrementAndGet();

    try {
      LOGGER.log(Level.FINEST, "Processing MQTT event {0} on topic {1} for device {2}.",
        new Object[]{event.getId(), event.getTopic(), event.getHardwareId()});
      if (initialized) {
        try {
          Point point = preparePoint(event.getHardwareId(), event.getPayload(), eventType);
          if (StringUtils.isNotBlank(influxDBConfiguration.getRetentionPolicy())) {
            influxDB.write(influxDBConfiguration.getDatabaseName(),
              influxDBConfiguration.getRetentionPolicy(), point);
          } else {
            influxDB.write(point);
          }
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, MessageFormat.format("Could not process MQTT event {0} on topic "
              + "{1} for device {2}.",
            new Object[]{event.getId(), event.getTopic(), event.getHardwareId()}), e);
        }
      } else {
        LOGGER.log(Level.WARNING,
          "Got an MQTT event {0} on topic {1} for device {2} before {3} being initialized.",
          new Object[]{event.getId(), event.getTopic(), event.getHardwareId(), sinkName});
      }
      LOGGER.log(Level.FINEST, "Finished processing MQTT event {0} on topic {1} for device {2}.",
        new Object[]{event.getId(), event.getTopic(), event.getHardwareId()});
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, MessageFormat.format("An unknown error happened during processing "
        + "of MQTT event {0}.", event.getId()), e);
    }

    eventsQueued.decrementAndGet();
  }

  public void disconnect() {
    LOGGER.log(Level.FINE, "Disconnecting {0}.", sinkName);
    if (influxDB != null) {
      influxDB.flush();
      influxDB.close();
    }
  }

  public float getPressure() {
    return (float) eventsQueued.intValue() / (float) influxDBConfiguration.getQueueSize();
  }

  @Override
  public String getFriendlyName() {
    return null;
  }

  private DataSinkMeasurement prepareOperationResult(QueryResult queryResult, String hardwareId,
    String measurement, String type) {
    // Return an empty response if no results are available.
    if (CollectionUtils.isEmpty(queryResult.getResults().get(0).getSeries())) {
      return null;
    }

    // Set default values about the measurement for which count is calculated.
    DataSinkMeasurement dataSinkMeasurement = new DataSinkMeasurement();
    dataSinkMeasurement.setHardwareId(hardwareId);
    dataSinkMeasurement.setType(type);
    dataSinkMeasurement.setMeasurement(measurement);

    // Remove unnecessary columns (such as 'time').
    final Series series = queryResult.getResults().get(0).getSeries().get(0);
    int timeColumnIndex = series.getColumns().indexOf(INFLUXDB_TIME_NAME);
    final List<String> columns = series.getColumns();
    columns.remove(timeColumnIndex);
    dataSinkMeasurement.setColumns(columns);

    // Set values (removing values of removed columns)
    final List<List<Object>> values = series.getValues();
    final List<Integer> indicesToRemove = Collections.singletonList(timeColumnIndex);
    values.stream().forEach(o ->
      indicesToRemove.stream().mapToInt(i -> i).forEach(o::remove)
    );
    dataSinkMeasurement.setValues(series.getValues());

    return dataSinkMeasurement;
  }

  private DataSinkMeasurement prepareMeasurementsResults(QueryResult queryResult) {
    // Return an empty response if no results are available.
    if (CollectionUtils.isEmpty(queryResult.getResults().get(0).getSeries())) {
      return null;
    }

    DataSinkMeasurement dataSinkMeasurement = new DataSinkMeasurement();
    final Series series = queryResult.getResults().get(0).getSeries().get(0);

    // Set the name of the measurement.
    dataSinkMeasurement.setMeasurement(series.getName());

    // Remove columns with duplicate values, such as the hardware ID and the measurement type.
    // Instead, add these values once in the DataSinkMeasurement.
    int hardwareIdColumnIndex = series.getColumns().indexOf(TAG_HARDWARE_ID_NAME);
    int measurementTypeColumnIndex = series.getColumns().indexOf(TAG_TYPE_NAME);
    dataSinkMeasurement
      .setHardwareId(series.getValues().get(0).get(hardwareIdColumnIndex).toString());
    dataSinkMeasurement
      .setType(series.getValues().get(0).get(measurementTypeColumnIndex).toString());
    final List<String> columns = series.getColumns();
    columns.remove(hardwareIdColumnIndex);
    measurementTypeColumnIndex = series.getColumns().indexOf(TAG_TYPE_NAME);
    columns.remove(measurementTypeColumnIndex);
    dataSinkMeasurement.setColumns(columns);

    // Set values (removing values of removed columns)
    final List<List<Object>> values = series.getValues();
    final List<Integer> indicesToRemove = Arrays
      .asList(hardwareIdColumnIndex, measurementTypeColumnIndex);
    values.stream().forEach(o ->
      indicesToRemove.stream().mapToInt(i -> i).forEach(o::remove)
    );
    dataSinkMeasurement.setValues(series.getValues());

    return dataSinkMeasurement;
  }

  private void setFromTo(WhereQueryImpl whereQuery, long fromDate, long toDate) {
    if (toDate == 0) {
      toDate = Instant.now().toEpochMilli();
    }
    whereQuery.and(gte(INFLUXDB_TIME_NAME, fromDate * 1000000));
    whereQuery.and(lte(INFLUXDB_TIME_NAME, toDate * 1000000));
  }

  private DataSinkMeasurement getOneOrdered(@NotNull String hardwareId, @NotNull String measurement,
    @NotNull String type, Ordering ordering, String... field) {
    Query query = select(field)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, type))
      .limit(1)
      .orderBy(ordering);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());
    return prepareMeasurementsResults(influxDB.query(query));
  }

  private void setPaging(WhereQueryImpl query, int page, int pageSize) {
    if (page > 0 && pageSize > 0) {
      query.limit(pageSize, page * pageSize);
    } else if (pageSize > 0) {
      query.limit(pageSize);
    }
  }

  @Override
  public DataSinkMeasurement getLast(@NotNull String hardwareId, @NotNull String measurement,
    @NotNull String type, String fields) {
    return getOneOrdered(hardwareId, measurement, type, desc(), fields);
  }

  @Override
  public DataSinkMeasurement getFirst(@NotNull String hardwareId, @NotNull String measurement,
    @NotNull String type, String fields) {
    return getOneOrdered(hardwareId, measurement, type, asc(), fields);
  }

  @Override
  public DataSinkMeasurement get(@NotNull String hardwareId, @NotNull String measurement,
    @NotNull String type, long fromDate, long toDate, int page, int pageSize, String fields) {
    WhereQueryImpl whereQuery = select(fields)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, type));
    setFromTo(whereQuery, fromDate, toDate);
    setPaging(whereQuery, page, pageSize);
    Query query = whereQuery.orderBy(desc());

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());
    return prepareMeasurementsResults(influxDB.query(query));
  }

  @Override
  public DataSinkMeasurement count(@NotNull String hardwareId, @NotNull String measurement,
    @NotNull String type, long fromDate, long toDate, String fields) {
    SelectionQueryImpl selectQuery = select();
    if (StringUtils.isNotBlank(fields)) {
      selectQuery.count(fields);
    } else {
      selectQuery.countAll();
    }
    WhereQueryImpl query = selectQuery
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, type));
    setFromTo(query, fromDate, toDate);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());

    return prepareOperationResult(influxDB.query(query), hardwareId, measurement, type);
  }

  @Override
  public DataSinkMeasurement max(@NotNull String hardwareId, @NotNull String measurement,
    @NotNull String type, long fromDate, long toDate, @NotNull String fields) {
    WhereQueryImpl query = select().max(fields)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, type));
    setFromTo(query, fromDate, toDate);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());

    return prepareOperationResult(influxDB.query(query), hardwareId, measurement, type);
  }

  @Override
  public DataSinkMeasurement min(@NotNull String hardwareId, @NotNull String measurement,
    @NotNull String type, long fromDate, long toDate, @NotNull String fields) {
    WhereQueryImpl query = select().min(fields)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, type));
    setFromTo(query, fromDate, toDate);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());

    return prepareOperationResult(influxDB.query(query), hardwareId, measurement, type);
  }

  @Override
  public DataSinkMeasurement sum(@NotNull String hardwareId, @NotNull String measurement,
    @NotNull String type, long fromDate, long toDate, @NotNull String fields) {
    WhereQueryImpl query = select().sum(fields)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, type));
    setFromTo(query, fromDate, toDate);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());

    return prepareOperationResult(influxDB.query(query), hardwareId, measurement, type);
  }

  @Override
  public DataSinkMeasurement average(@NotNull String hardwareId, @NotNull String measurement,
    @NotNull String type, long fromDate, long toDate, @NotNull String fields) {
    WhereQueryImpl query = select().mean(fields)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, type));
    setFromTo(query, fromDate, toDate);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());

    return prepareOperationResult(influxDB.query(query), hardwareId, measurement, type);
  }

  public String getSinkName() {
    return sinkName;
  }

}
