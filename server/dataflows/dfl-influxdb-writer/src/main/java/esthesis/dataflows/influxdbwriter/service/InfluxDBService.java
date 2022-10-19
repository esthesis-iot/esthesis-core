package esthesis.dataflows.influxdbwriter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import esthesis.dataflow.common.messages.DflUtils;
import esthesis.dataflow.common.messages.EsthesisMessage;
import esthesis.dataflow.common.messages.PayloadParser;
import esthesis.dataflows.influxdbwriter.config.AppConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@ApplicationScoped
public class InfluxDBService {

  private final static String INFLUXDB_TAG_HARDWARE_ID = "hardwareId";

  @Inject
  private DflUtils dflUtils;
  @Inject
  private AppConfig config;
  private InfluxDBClient influxDBClient;

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

  public void process(Exchange exchange) throws JsonProcessingException {
    // Parse the message and get the payload.
    EsthesisMessage esthesisMessage = dflUtils.parseEsthesisMessage(exchange);
    String payload = esthesisMessage.getPayload();

    // Create InfluxDB Points for each line of the payload.
    List<Point> points = new ArrayList<>();
    Arrays.stream(payload.split("\n")).forEach(line -> {
      // Parse payload.
      PayloadParser payloadParser = PayloadParser.parse(line);
      log.debug("Parsed payload: '{}'.", payloadParser);

      // Check data validity.
      if (payloadParser == null) {
        log.warn("Esthesis payload does not conform to '$measurement"
                + ".field=value' format, payload received: '{}'.",
            StringUtils.abbreviate(line, "...", 64));
        return;
      }

      // Create the InfluxDB Point.
      Point point =
          Point.measurement(payloadParser.getMeasurement())
              .time(System.currentTimeMillis(), WritePrecision.MS)
              .addTag(INFLUXDB_TAG_HARDWARE_ID,
                  exchange.getProperty(DflUtils.ESTHESIS_CAMEL_PROP_HARDWARE_ID,
                      String.class));

      if (payloadParser.getValue() instanceof Boolean) {
        point.addField(payloadParser.getField(),
            (Boolean) payloadParser.getValue());
        log.debug("Identified value '{}' as boolean.",
            payloadParser.getValue());
      } else if (payloadParser.getValue() instanceof Number) {
        point.addField(payloadParser.getField(),
            (Number) payloadParser.getValue());
        log.debug("Identified value '{}' as number.", payloadParser.getValue());
      } else {
        point.addField(payloadParser.getField(),
            (String) payloadParser.getValue());
        log.debug("Identified value '{}' as string.", payloadParser.getValue());
      }

      // Add the Point to the list of Points collected.
      points.add(point);
    });

    if (points.size() == 0) {
      log.warn("No data could be found in payload '{}'.",
          StringUtils.abbreviate(payload, "...", 64));
    } else if (points.size() == 1) {
      WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
      writeApi.writePoint(points.get(0));
    } else {
      WriteApi writeApi = influxDBClient.makeWriteApi(
          WriteOptions.builder().build());
      writeApi.writePoints(points);
    }
  }
}
