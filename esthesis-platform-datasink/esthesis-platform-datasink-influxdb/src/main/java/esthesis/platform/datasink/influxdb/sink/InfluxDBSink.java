package esthesis.platform.datasink.influxdb.sink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.extension.config.AppConstants.MqttPayload;
import esthesis.platform.datasink.influxdb.config.InfluxDBConfiguration;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point.Builder;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class InfluxDBSink {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(InfluxDBSink.class.getName());
  private InfluxDB influxDB;
  private boolean initialized;
  private InfluxDBConfiguration influxDBConfiguration;
  private String sinkName;
  private String tagName;
  // The name of the tag to indicate the type of the measurement.
  public static final String TAG_TYPE_NAME = "type";
  private static ObjectMapper mapper = new ObjectMapper();

  InfluxDBSink(String configuration, String sinkName, String tagName) {
    LOGGER.log(Level.FINE, "Instantiating {0}.", sinkName);
    this.sinkName = sinkName;
    this.tagName = tagName;

    // Parse configuration.
    influxDBConfiguration = new Yaml(new Constructor(InfluxDBConfiguration.class)).load(configuration);

    // Connect to database.
    influxDB = connect(influxDBConfiguration);

    // Create the database if not exists.
    createDatabase(influxDB, influxDBConfiguration);

    initialized = true;
    LOGGER.log(Level.FINE, "Instantiated {0}.", sinkName);
  }

  private void createDatabase(InfluxDB influxDB, InfluxDBConfiguration influxDBConfiguration) {
    if (!influxDB.databaseExists(influxDBConfiguration.getDatabaseName())) {
      // Create and set database as default.
      influxDB.createDatabase(influxDBConfiguration.getDatabaseName());
      influxDB.setDatabase(influxDBConfiguration.getDatabaseName());

      // Create a retention policy for this database.
      influxDB.createRetentionPolicy(
          influxDBConfiguration.getRetentionPolicyName(),
          influxDBConfiguration.getDatabaseName(),
          influxDBConfiguration.getRetentionPolicyDuration(),
          influxDBConfiguration.getShardDuration(),
          influxDBConfiguration.getReplicationFactor(),
          true);
      influxDB.setRetentionPolicy(influxDBConfiguration.getRetentionPolicyName());
      influxDB.enableBatch(BatchOptions.DEFAULTS);
    }
  }

  private InfluxDB connect(InfluxDBConfiguration influxDBConfiguration) {
    return InfluxDBFactory.connect(
        influxDBConfiguration.getDatabaseUrl(),
        influxDBConfiguration.getUsername(),
        influxDBConfiguration.getPassword());
  }

  private Builder preparePoint(byte[] mqttPayload, String tagName) throws IOException {
    // Read the payload of the MQTT message.
    final JsonNode jsonNode = mapper.readTree(mqttPayload);

    // Prepare an InfluxDB Point with the name of the measurement.
    String metricName = jsonNode.get(MqttPayload.METRIC_KEYNAME).asText();
    final Builder pointBuilder = org.influxdb.dto.Point.measurement(metricName);

    // Add a tag to the Point.
    pointBuilder.tag(TAG_TYPE_NAME, tagName);

    // If a timestamp is provided use the provided timestamp, otherwise create a timestamp.
    if (jsonNode.get(MqttPayload.TIMESTAMP_KEYNAME) != null) {
      pointBuilder.time(jsonNode.get(MqttPayload.TIMESTAMP_KEYNAME).asLong(), TimeUnit.MILLISECONDS);
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

    return pointBuilder;
  }

  public void processEvent(byte[] mqttEventPayload, String eventId, String topic) {
    LOGGER.log(Level.FINEST, "Processing MQTT event {0} on topic {1}.", new Object[]{eventId, topic});
    if (initialized) {
      try {
        influxDB.write(influxDBConfiguration.getDatabaseName(), influxDBConfiguration.getRetentionPolicyName(),
            preparePoint(mqttEventPayload, tagName).build());
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Could not process MQTT event {0} on topic {1}.", new Object[]{eventId, topic});
      }
    } else {
      LOGGER.log(Level.WARNING, "Got an MQTT event {0} on topic {1} before {2} being initialized.",
          new Object[]{eventId, topic, sinkName});
    }
    LOGGER.log(Level.FINEST, "Finished processing MQTT event {0} on topic {1}.", new Object[]{eventId, topic});
  }

  public void disconnect() {
    LOGGER.log(Level.FINE, "Disconnecting {0}.", sinkName);
    if (influxDB != null) {
      influxDB.flush();
      influxDB.close();
    }
  }
}
