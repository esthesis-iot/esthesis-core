package esthesis.dataflows.influxdbwriter.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import esthesis.dataflow.common.DflUtils.VALUE_TYPE;
import esthesis.dataflow.common.parser.EsthesisMessage;
import esthesis.dataflows.influxdbwriter.config.AppConfig;
import java.time.Instant;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Slf4j
@ApplicationScoped
public class InfluxDBService {

  // The name of the InfluxDB tag to use to for the hardware id of each device.
  private final static String INFLUXDB_TAG_HARDWARE_ID = "hardwareId";

  @Inject
  AppConfig config;
  InfluxDBClient influxDBClient;

  @PostConstruct
  void init() {
    reconnectClient();
  }

  @PreDestroy
  void destroy() {
    if (influxDBClient != null) {
      log.debug("Shutting down client, closing existing InfluxDB connection.");
      influxDBClient.close();
    }
  }

  private void reconnectClient() {
    if (influxDBClient != null) {
      log.debug("Reconnect client, closing existing InfluxDB connection.");
      influxDBClient.close();
    }

    log.debug("Connecting to InfluxDB at '{}', org '{}', and bucket '{}'.",
        config.influxUrl(), config.influxOrg(), config.influxBucket());
    influxDBClient = InfluxDBClientFactory
        .create(config.influxUrl(), config.influxToken().toCharArray(),
            config.influxOrg(), config.influxBucket());
  }

  public void process(Exchange exchange) {
    // Get the message from the exchange.
    EsthesisMessage esthesisMessage = exchange.getIn()
        .getBody(EsthesisMessage.class);

    // Create an InfluxDB point for the message.
    Point point = new Point(esthesisMessage.getPayload().getCategory());
    point.addTag(INFLUXDB_TAG_HARDWARE_ID, exchange.getIn()
        .getHeader(KafkaConstants.KEY, String.class));
    if (esthesisMessage.getPayload().getTimestamp() != null) {
      point.time(Instant.parse(esthesisMessage.getPayload().getTimestamp()),
          WritePrecision.NS);
    } else {
      if (esthesisMessage.getSeenAt() != null) {
        point.time(Instant.parse(esthesisMessage.getSeenAt()),
            WritePrecision.MS);
      } else {
        point.time(System.currentTimeMillis(), WritePrecision.MS);
      }
    }

    // Set the values of the point.
    esthesisMessage.getPayload().getValues().forEach((keyValue) -> {
      String name = keyValue.getName();
      String value = keyValue.getValue();

      switch (VALUE_TYPE.valueOf(keyValue.getValueType())) {
        case STRING -> point.addField(name, value);
        case BOOLEAN -> point.addField(name, BooleanUtils.toBoolean(value));
        case BYTE -> point.addField(name, NumberUtils.toByte(value));
        case SHORT -> point.addField(name, NumberUtils.toShort(value));
        case INTEGER -> point.addField(name, NumberUtils.toInt(value));
        case LONG -> point.addField(name, NumberUtils.toLong(value));
        case FLOAT -> point.addField(name, NumberUtils.toFloat(value));
        case DOUBLE -> point.addField(name, NumberUtils.toDouble(value));
        default -> log.warn("Unknown value type '{}', ignoring value '{}'.",
            keyValue.getValueType(), value);
      }
    });

    // Write the point to InfluxDB.
    log.debug("Writing point '{}'.", point.toLineProtocol());
    WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
    writeApi.writePoint(point);
  }
}
