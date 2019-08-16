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
import esthesis.extension.datasink.dto.DataSinkQueryResult;
import esthesis.extension.datasink.dto.FieldDTO;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BoundParameterQuery.QueryBuilder;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class InfluxDBDataSink implements DataSink {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(InfluxDBDataSink.class.getName());
  private InfluxDB influxDB;
  private boolean initialized;
  private InfluxDBConfiguration influxDBConfiguration;
  private String sinkName;
  private String mqttTopic;
  // The name of the tag to indicate the type of the measurement.
  private static final String TAG_TYPE_NAME = "type";
  // The name of the tag to indicate the hardware ID for which a measurement was recorded.
  private static final String TAG_HARDWARE_ID_NAME = "hardwareId";
  // The default name of the field when no specific name is provided for a measurement.
  private static final String NO_NAME_VALUE = "value";
  // The default InfluxDB name used for time column.
  private static final String INFLUXDB_TIME_NAME = "time";

  private static ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(
    Include.NON_EMPTY);
  private static AtomicInteger eventsQueued = new AtomicInteger(0);

  InfluxDBDataSink(String configuration, String sinkName, String mqttTopic) {
    LOGGER.log(Level.FINE, "Instantiating {0}.", sinkName);
    this.sinkName = sinkName;
    this.mqttTopic = mqttTopic;

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

  private Point preparePoint(String hardwareId, byte[] mqttPayload, String mqttTopic)
  throws IOException {
    // Read the payload of the MQTT message.
    final JsonNode jsonNode = mapper.readTree(mqttPayload);

    // Prepare an InfluxDB Point with the name of the measurement.
    String metricName = jsonNode.get(Mqtt.MqttPayload.METRIC_KEYNAME).asText();
    final Builder pointBuilder = org.influxdb.dto.Point.measurement(metricName);

    // Add a tag to the Point for the type of the message.
    pointBuilder.tag(TAG_TYPE_NAME, mqttTopic.replaceAll("/", ""));

    // Add a tag to the Point for the device ID.
    pointBuilder.tag(TAG_HARDWARE_ID_NAME, hardwareId);

    // If a timestamp is provided use the provided timestamp, otherwise create a timestamp.
    if (jsonNode.get(Mqtt.MqttPayload.TIMESTAMP_KEYNAME) != null) {
      String ts = jsonNode.get(Mqtt.MqttPayload.TIMESTAMP_KEYNAME).asText();
      if (ts.length() == 10) {
        pointBuilder.time(Long.valueOf(ts), TimeUnit.SECONDS);
      } else if (ts.length() == 13) {
        pointBuilder.time(Long.valueOf(ts), TimeUnit.MILLISECONDS);
      } else if (ts.length() == 16) {
        pointBuilder.time(Long.valueOf(ts), TimeUnit.MICROSECONDS);
      }
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

  private List<FieldDTO> getFieldsForMeasurement(String database, String measurement) {
    if (measurement != null && !measurement.matches("[A-Za-z0-9_]+")) {
      throw new SecurityException("Measurement value should be alphanumeric.");
    }
    QueryBuilder queryBuilder;
    if (measurement == null) {
      queryBuilder = QueryBuilder.newQuery("SHOW FIELD KEYS on " + database);
    } else {
      queryBuilder = QueryBuilder.newQuery("SHOW FIELD KEYS from " + measurement);
    }
    queryBuilder
      .forDatabase(influxDBConfiguration.getDatabaseName());
    final QueryResult results = influxDB.query(queryBuilder.create());

    if (!CollectionUtils.isEmpty(results.getResults().get(0).getSeries())) {
      return results.getResults().get(0).getSeries().stream().map(s -> s.getValues().stream()
        .map(o -> new FieldDTO(s.getName() + "." + (String) o.get(0), (String) o.get(1)))
        .collect(Collectors.toList())).collect(Collectors.toList()).stream().flatMap(List::stream)
        .collect(
          Collectors.toList());
    } else {
      return new ArrayList<>();
    }
  }

  public void processEvent(MQTTDataEvent event) {
    eventsQueued.incrementAndGet();

    try {
      LOGGER.log(Level.FINEST, "Processing MQTT event {0} on topic {1} for device {2}.",
        new Object[]{event.getId(), event.getTopic(), event.getHardwareId()});
      if (initialized) {
        try {
          Point point = preparePoint(event.getHardwareId(), event.getPayload(), mqttTopic);
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

  private DataSinkQueryResult prepareOperationResult(QueryResult queryResult, String hardwareId) {
    DataSinkQueryResult dataSinkMeasurement = new DataSinkQueryResult();

    // Return an empty response if no results are available.
    if (!CollectionUtils.isEmpty(queryResult.getResults().get(0).getSeries())) {
      dataSinkMeasurement.setHardwareId(hardwareId);
      //    dataSinkMeasurement.setType(eventType);
      //    dataSinkMeasurement.setMeasurements(measurement);

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
    }

    return dataSinkMeasurement;
  }

  private DataSinkQueryResult prepareMeasurementsResults(QueryResult queryResult) {
    DataSinkQueryResult dataSinkQueryResult = new DataSinkQueryResult();

    // Get the series contained on this result set.
    final List<Series> series = queryResult.getResults().get(0).getSeries();

    // Fill-in results.
    if (!CollectionUtils.isEmpty(series)) {
      final Series seriesData = series.get(0);

      dataSinkQueryResult.setColumns(seriesData.getColumns());
      dataSinkQueryResult.setValues(seriesData.getValues());
    }

    return dataSinkQueryResult;
  }

  private void setFromTo(WhereQueryImpl whereQuery, long fromDate, long toDate) {
    if (toDate == 0) {
      toDate = Instant.now().toEpochMilli();
    }
    whereQuery.and(gte(INFLUXDB_TIME_NAME, fromDate * 1000000));
    whereQuery.and(lte(INFLUXDB_TIME_NAME, toDate * 1000000));
  }

  private DataSinkQueryResult getOneOrdered(@NotNull String hardwareId,
    @NotEmpty String measurement, @NotNull String type, Ordering ordering,
    @NotEmpty String[] field) {
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
  public DataSinkQueryResult getLast(@NotNull String hardwareId, @NotEmpty String measurement,
    @NotNull String eventType, @NotEmpty String[] fields) {
    return getOneOrdered(hardwareId, measurement, eventType, desc(), fields);
  }


  @Override
  public DataSinkQueryResult getFirst(@NotNull String hardwareId, @NotEmpty String measurement,
    @NotNull String eventType, @NotNull String[] fields) {
    return getOneOrdered(hardwareId, measurement, eventType, asc(), fields);
  }

  @Override
  public DataSinkQueryResult get(@NonNull String hardwareId, @NotEmpty String measurement,
    @NotNull String eventType, long fromDate, long toDate, int page, int pageSize,
    @NotEmpty String[] fields) {
    WhereQueryImpl whereQuery = select(fields)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, eventType));
    setFromTo(whereQuery, fromDate, toDate);
    setPaging(whereQuery, page, pageSize);
    Query query = whereQuery.orderBy(desc());

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());
    return prepareMeasurementsResults(influxDB.query(query))
      .setHardwareId(hardwareId)
      .setMeasurement(measurement);
  }

  @Override
  public DataSinkQueryResult count(@NotNull String hardwareId, @NotEmpty String measurement,
    @NotNull String eventType, long fromDate, long toDate, String field) {
    SelectionQueryImpl selectQuery = select();
    if (StringUtils.isNotBlank(field)) {
      selectQuery.count(field);
    } else {
      selectQuery.countAll();
    }
    WhereQueryImpl query = selectQuery
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, eventType));
    setFromTo(query, fromDate, toDate);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());

    return prepareOperationResult(influxDB.query(query), hardwareId)
      .setHardwareId(hardwareId)
      .setMeasurement(measurement);
  }

  @Override
  public DataSinkQueryResult max(@NotNull String hardwareId, @NotEmpty String measurement,
    @NotNull String eventType, long fromDate, long toDate, @NotEmpty String field) {
    WhereQueryImpl query = select().max(field)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, eventType));
    setFromTo(query, fromDate, toDate);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());

    return prepareOperationResult(influxDB.query(query), hardwareId)
      .setHardwareId(hardwareId)
      .setMeasurement(measurement);
  }

  @Override
  public DataSinkQueryResult min(@NotNull String hardwareId, @NotEmpty String measurement,
    @NotNull String eventType, long fromDate, long toDate, @NotEmpty String field) {
    WhereQueryImpl query = select().min(field)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, eventType));
    setFromTo(query, fromDate, toDate);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());

    return prepareOperationResult(influxDB.query(query), hardwareId)
      .setHardwareId(hardwareId)
      .setMeasurement(measurement);
  }

  @Override
  public DataSinkQueryResult sum(@NotNull String hardwareId, @NotEmpty String measurement,
    @NotNull String eventType, long fromDate, long toDate, @NotEmpty String field) {
    WhereQueryImpl query = select().sum(field)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, eventType));
    setFromTo(query, fromDate, toDate);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());

    return prepareOperationResult(influxDB.query(query), hardwareId)
      .setHardwareId(hardwareId)
      .setMeasurement(measurement);
  }

  @Override
  public DataSinkQueryResult average(@NotNull String hardwareId, @NotEmpty String measurement,
    @NotNull String eventType, long fromDate, long toDate, @NotEmpty String field) {
    WhereQueryImpl query = select().mean(field)
      .from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId))
      .and(eq(TAG_TYPE_NAME, eventType));
    setFromTo(query, fromDate, toDate);

    LOGGER.log(Level.FINEST, "Executing query: {0}. ", query.getCommand());

    return prepareOperationResult(influxDB.query(query), hardwareId)
      .setHardwareId(hardwareId)
      .setMeasurement(measurement);
  }

  @Override
  public List<FieldDTO> getFields() {
    return getFieldsForMeasurement(this.influxDBConfiguration.getDatabaseName(), null);
  }

  @Override
  public List<FieldDTO> getFieldsForMeasurement(String measurement) {
    return getFieldsForMeasurement(null, measurement);
  }

  public String getSinkName() {
    return sinkName;
  }

}
