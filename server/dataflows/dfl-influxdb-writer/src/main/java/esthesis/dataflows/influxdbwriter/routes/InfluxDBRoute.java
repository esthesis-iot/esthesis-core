package esthesis.dataflows.influxdbwriter.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflows.influxdbwriter.config.AppConfig;
import esthesis.dataflows.influxdbwriter.service.InfluxDBService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.model.dataformat.AvroDataFormat;

@Slf4j
@ApplicationScoped
public class InfluxDBRoute extends RouteBuilder {

  @Inject
  InfluxDBService influxDBService;

  @Inject
  AppConfig config;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-influxdb-writer");

    // Configure concurrency.
    ComponentsBuilderFactory.seda()
        .queueSize(config.queueSize())
        .defaultPollTimeout(config.pollTimeout())
        .concurrentConsumers(config.consumers())
        .register(getContext(), "seda");

    // @formatter:off
    if (config.kafkaTelemetryTopic().isPresent()) {
      String kafkaTopic = config.kafkaTelemetryTopic().get();
      log.info("Setting up route from Kafka topic '{}' to InfluxDB '{}' "
              + "bucket '{}'.", kafkaTopic, config.influxUrl(), config.influxBucket());
     from("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl() +
          (config.kafkaGroup().isPresent() ?
          "&groupId=" + config.kafkaGroup().get() : ""))
         .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
         .to("seda:telemetry");
      from("seda:telemetry")
         .bean(influxDBService, "process");
     }

    if (config.kafkaMetadataTopic().isPresent()) {
      String kafkaTopic = config.kafkaMetadataTopic().get();
      log.info("Setting up route from Kafka topic '{}' to InfluxDB '{}' "
              + "bucket '{}'.", kafkaTopic, config.influxUrl(), config.influxBucket());
      from("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl() +
          (config.kafkaGroup().isPresent() ?
              "&groupId=" + config.kafkaGroup().get() : ""))
          .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
          .to("seda:metadata");
      from("seda:metadata")
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
