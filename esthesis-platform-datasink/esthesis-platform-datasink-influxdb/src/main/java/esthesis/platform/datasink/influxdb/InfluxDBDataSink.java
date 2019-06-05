package esthesis.platform.datasink.influxdb;

import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.eq;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.select;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.extension.datasink.DataSink;
import esthesis.extension.datasink.DataSinkDataPoint;
import esthesis.extension.datasink.MQTTDataEvent;
import esthesis.extension.datasink.config.AppConstants.Mqtt;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult.Result;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
  public static final String TAG_HARDWARE_ID_NAME = "hardwareId";
  // The default name of the field when no specific name is provided for a measurement.
  public static final String NO_NAME_VALUE = "value";
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
        + "of MQTT event {0}.", event.getId()));
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

  @Override
  public List<DataSinkDataPoint> getDataPoint(String hardwareId, String measurement,
    Optional<String> field) {
    return null;
  }

  @Override
  public List<DataSinkDataPoint> getDataPoints(String hardwareId, String measurement,
    Instant fromDate, Instant toDate, String... fields) {
    return null;
  }

  @Override
  public List<DataSinkDataPoint> getDataPoints(String hardwareId, String measurement,
    Instant fromDate, Instant toDate, int resultsPage, int resultsPageSize, String... fields) {
    return null;
  }

  @Override
  public long countDataPoints(String hardwareId, String measurement, Instant fromDate,
    Instant toDate, String... fields) {
    return 0;
  }

  @Override
  public long maxDataPoint(String hardwareId, String measurement, Instant fromDate, Instant toDate,
    Optional<String> field) {
    return 0;
  }

  @Override
  public long minDataPoint(String hardwareId, String measurement, Instant fromDate, Instant toDate,
    Optional<String> field) {
    return 0;
  }

  @Override
  public long averageDataPoint(String hardwareId, String measurement, Instant fromDate,
    Instant toDate, Optional<String> field) {
    return 0;
  }

  public String getSinkName() {
    return sinkName;
  }

  public void getData(String hardwareId, String measurement) {
    Query query = select().from(influxDBConfiguration.getDatabaseName(), measurement)
      .where(eq(TAG_HARDWARE_ID_NAME, hardwareId));
    final List<Result> results = influxDB.query(query).getResults();
    System.out.println(results);

  }
}
