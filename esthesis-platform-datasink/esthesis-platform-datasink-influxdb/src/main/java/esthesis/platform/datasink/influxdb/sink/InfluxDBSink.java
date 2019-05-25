package esthesis.platform.datasink.influxdb.sink;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.extension.config.AppConstants.MqttPayload;
import esthesis.platform.datasink.influxdb.config.InfluxDBConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class InfluxDBSink {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(InfluxDBSink.class.getName());
  private InfluxDB influxDB;
  private boolean initialized;
  private InfluxDBConfiguration influxDBConfiguration;
  private String sinkName;
  private String eventType;
  // The name of the tag to indicate the type of the measurement.
  public static final String TAG_TYPE_NAME = "type";
  public static final String TAG_HARDWARE_ID_NAME = "hardwareId";
  private static ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(
    Include.NON_EMPTY);
  private static AtomicInteger eventsQueued = new AtomicInteger(0);

  InfluxDBSink(String configuration, String sinkName, String eventType) {
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
    String metricName = jsonNode.get(MqttPayload.METRIC_KEYNAME).asText();
    final Builder pointBuilder = org.influxdb.dto.Point.measurement(metricName);

    // Add a tag to the Point for the type of the message.
    pointBuilder.tag(TAG_TYPE_NAME, eventType);

    // Add a tag to the Point for the device ID.
    pointBuilder.tag(TAG_HARDWARE_ID_NAME, hardwareId);

    // If a timestamp is provided use the provided timestamp, otherwise create a timestamp.
    if (jsonNode.get(MqttPayload.TIMESTAMP_KEYNAME) != null) {
      pointBuilder
        .time(jsonNode.get(MqttPayload.TIMESTAMP_KEYNAME).asLong(), TimeUnit.MILLISECONDS);
    } else {
      pointBuilder.time(Instant.now().toEpochMilli(), TimeUnit.MILLISECONDS);
    }

    // Find the values of the payload.
    final JsonNode values = jsonNode.get(MqttPayload.VALUES_KEYNAME);
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
        pointBuilder.addField(metricName, values.asLong());
      } else if (values.isDouble() || values.isFloat()) {
        pointBuilder.addField(metricName, values.asDouble());
      } else if (values.isTextual()) {
        pointBuilder.addField(metricName, values.asText());
      } else if (values.isBoolean()) {
        pointBuilder.addField(metricName, values.asBoolean());
      } else if (values.isInt() || values.isShort()) {
        pointBuilder.addField(metricName, values.asInt());
      } else if (values.isBigInteger()) {
        pointBuilder.addField(metricName, BigInteger.valueOf(values.asLong()));
      } else if (values.isBigDecimal()) {
        pointBuilder.addField(metricName, BigDecimal.valueOf(values.asDouble()));
      }
    }

    return pointBuilder.build();
  }

  public void processEvent(String hardwareId, byte[] mqttEventPayload, String eventId,
    String topic) {
    eventsQueued.incrementAndGet();

    try {
      LOGGER.log(Level.FINEST, "Processing MQTT event {0} on topic {1} for device {2}.",
        new Object[]{eventId, topic, hardwareId});
      if (initialized) {
        try {
          Point point = preparePoint(hardwareId, mqttEventPayload, eventType);
          if (StringUtils.isNotBlank(influxDBConfiguration.getRetentionPolicy())) {
            influxDB.write(influxDBConfiguration.getDatabaseName(),
              influxDBConfiguration.getRetentionPolicy(), point);
          } else {
            influxDB.write(point);
          }
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, MessageFormat.format("Could not process MQTT event {0} on topic "
              + "{1} for device {2}.",
            new Object[]{eventId, topic, hardwareId}), e);
        }
      } else {
        LOGGER.log(Level.WARNING,
          "Got an MQTT event {0} on topic {1} for device {2} before {3} being initialized.",
          new Object[]{eventId, topic, hardwareId, sinkName});
      }
      LOGGER.log(Level.FINEST, "Finished processing MQTT event {0} on topic {1} for device {2}.",
        new Object[]{eventId, topic, hardwareId});
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, MessageFormat.format("An unknown error happened during processing "
        + "of MQTT event {0}.", eventId), 3);
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

  public String getSinkName() {
    return sinkName;
  }
}
