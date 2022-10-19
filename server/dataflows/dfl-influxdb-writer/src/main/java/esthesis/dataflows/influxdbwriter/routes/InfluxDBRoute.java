package esthesis.dataflows.influxdbwriter.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflow.common.messages.DflUtils;
import esthesis.dataflows.influxdbwriter.config.AppConfig;
import esthesis.dataflows.influxdbwriter.service.InfluxDBService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

@Slf4j
@ApplicationScoped
public class InfluxDBRoute extends RouteBuilder {

  @Inject
  InfluxDBService influxDBService;

  @Inject
  DflUtils dflUtils;

  @Inject
  AppConfig config;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-influxdb-writer");

    // @formatter:off
    if (config.kafkaTelemetryTopic().isPresent()) {
      log.info("Setting up route from Kafka topic '{}' to InfluxDB '{}' "
              + "bucket '{}'.",
          config.kafkaTelemetryTopic().get(), config.influxUrl(), config.influxBucket());
     from("kafka:" + config.kafkaTelemetryTopic().get() +
        "?brokers=" + config.kafkaClusterUrl() +
        (config.kafkaGroup().isPresent() ?
        "&groupId=" + config.kafkaGroup().get() : ""))
         .bean(dflUtils, "extractHardwareIdFromKafka")
         .bean(influxDBService, "process");
     }

    if (config.kafkaMetadataTopic().isPresent()) {
      log.info("Setting up route from Kafka topic '{}' to InfluxDB '{}' "
              + "bucket '{}'.",
          config.kafkaTelemetryTopic().get(), config.influxUrl(), config.influxBucket());
     from("kafka:" + config.kafkaMetadataTopic().get() +
        "?brokers=" + config.kafkaClusterUrl() +
        (config.kafkaGroup().isPresent() ?
        "&groupId=" + config.kafkaGroup().get() : ""))
         .bean(dflUtils, "extractHardwareIdFromKafka")
         .bean(influxDBService, "process");
     }
    // @formatter:on

    // Display a warning if no Kafka topics are configured.
    if (config.kafkaTelemetryTopic().isEmpty() && config.kafkaMetadataTopic()
        .isEmpty()) {
      log.warn("No Kafka topics are configured.");
    }
  }
}
