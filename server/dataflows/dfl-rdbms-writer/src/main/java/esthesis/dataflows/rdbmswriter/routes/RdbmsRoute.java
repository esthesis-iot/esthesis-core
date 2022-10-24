package esthesis.dataflows.rdbmswriter.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflow.common.DflUtils;
import esthesis.dataflows.rdbmswriter.config.AppConfig;
import esthesis.dataflows.rdbmswriter.service.RdbmsService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

@Slf4j
@ApplicationScoped
public class RdbmsRoute extends RouteBuilder {

  @Inject
  RdbmsService rdbmsService;

  @Inject
  DflUtils dflUtils;

  @Inject
  AppConfig config;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-influxdb-writer");

    // @formatter:off
    if (config.kafkaTelemetryTopic().isPresent()) {
      log.info("Setting up route from Kafka topic '{}' to MySQL.",
          config.kafkaTelemetryTopic().get());
     from("kafka:" + config.kafkaTelemetryTopic().get() +
        "?brokers=" + config.kafkaClusterUrl() +
        (config.kafkaGroup().isPresent() ?
        "&groupId=" + config.kafkaGroup().get() : ""))
//         .bean(dflUtils, "extractHardwareIdFromKafka")
         .bean(rdbmsService, "process");
     }

    if (config.kafkaMetadataTopic().isPresent()) {
      log.info("Setting up route from Kafka topic '{}' to MySQL.",
          config.kafkaTelemetryTopic().get());
     from("kafka:" + config.kafkaMetadataTopic().get() +
        "?brokers=" + config.kafkaClusterUrl() +
        (config.kafkaGroup().isPresent() ?
        "&groupId=" + config.kafkaGroup().get() : ""))
//         .bean(dflUtils, "extractHardwareIdFromKafka")
         .bean(rdbmsService, "process");
     }
    // @formatter:on

    // Display a warning if no Kafka topics are configured.
    if (config.kafkaTelemetryTopic().isEmpty() && config.kafkaMetadataTopic()
        .isEmpty()) {
      log.warn("No Kafka topics are configured.");
    }
  }
}
