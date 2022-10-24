package esthesis.dataflows.rdbmswriter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import esthesis.dataflow.common.DflUtils;
import esthesis.dataflows.rdbmswriter.config.AppConfig;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;

@Slf4j
@ApplicationScoped
public class RdbmsService {

  private final static String INFLUXDB_TAG_HARDWARE_ID = "hardwareId";

  @Inject
  private DflUtils dflUtils;
  @Inject
  private AppConfig config;

  @PostConstruct
  void init() {
    reconnectClient();
  }

  @PreDestroy
  void destroy() {
//    if (influxDBClient != null) {
//      log.debug("Shutting down client, closing existing InfluxDB connection.");
//      influxDBClient.close();
//    }
  }

  private void reconnectClient() {
//    if (influxDBClient != null) {
//      log.debug("Reconnect client, closing existing InfluxDB connection.");
//      influxDBClient.close();
//    }
//
//    log.debug("Connecting to InfluxDB at '{}', org '{}', and bucket '{}'.",
//        config.influxUrl(), config.influxOrg(), config.influxBucket());
//    influxDBClient = InfluxDBClientFactory
//        .create(config.influxUrl(), config.influxToken().toCharArray(),
//            config.influxOrg(), config.influxBucket());
  }

  public void process(Exchange exchange) throws JsonProcessingException {
//    // Parse the message and get the payload.
//    EsthesisMessageOld esthesisMessageOld = dflUtils.parseEsthesisMessage(
//        exchange);
//    String payload = esthesisMessageOld.getPayload();
//
//    // Create InfluxDB Points for each line of the payload.
////    List<Point> points = new ArrayList<>();
//    Arrays.stream(payload.split("\n")).forEach(line -> {
//      // Parse payload.
//      PayloadParser payloadParser = PayloadParser.parse(line);
//      log.debug("Parsed payload: '{}'.", payloadParser);
//
//      // Check data validity.
//      if (payloadParser == null) {
//        log.warn("Esthesis payload does not conform to '$measurement"
//                + ".field=value' format, payload received: '{}'.",
//            StringUtils.abbreviate(line,
//                DflUtils.MESSAGE_LOG_ABBREVIATION_LENGTH));
//        return;
//      }
//
//      // Add the Point to the list of Points collected.
////      points.add(point);
//    });

//    if (points.size() == 0) {
//      log.warn("No data could be found in payload '{}'.",
//          StringUtils.abbreviate(payload, "...", 64));
//    } else if (points.size() == 1) {
//      WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
//      writeApi.writePoint(points.get(0));
//    } else {
//      WriteApi writeApi = influxDBClient.makeWriteApi(
//          WriteOptions.builder().build());
//      writeApi.writePoints(points);
//    }
  }
}
