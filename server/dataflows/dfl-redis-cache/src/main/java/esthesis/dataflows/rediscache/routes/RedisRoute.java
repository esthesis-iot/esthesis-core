package esthesis.dataflows.rediscache.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflows.rediscache.config.AppConfig;
import esthesis.dataflows.rediscache.service.RedisService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.model.dataformat.AvroDataFormat;

@Slf4j
@ApplicationScoped
public class RedisRoute extends RouteBuilder {

  @Inject
  RedisService redisService;

  @Inject
  AppConfig config;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-redis-cache");

    // Configure concurrency.
    ComponentsBuilderFactory.seda()
        .queueSize(config.queueSize())
        .defaultPollTimeout(config.pollTimeout())
        .concurrentConsumers(config.consumers())
        .register(getContext(), "seda");

    // @formatter:off
    if (config.kafkaTelemetryTopic().isPresent()) {
      String kafkaTopic = config.kafkaTelemetryTopic().get();
      log.info("Setting up route from Kafka topic '{}' to Redis '{}'.",
          kafkaTopic, config.redisUrl());
     from("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl() +
          (config.kafkaGroup().isPresent() ?
          "&groupId=" + config.kafkaGroup().get() : ""))
         .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
         .to("seda:telemetry");
      from("seda:telemetry")
         .bean(redisService, "process");
     }

    if (config.kafkaMetadataTopic().isPresent()) {
      String kafkaTopic = config.kafkaMetadataTopic().get();
      log.info("Setting up route from Kafka topic '{}' to Redis '{}'.",
          kafkaTopic, config.redisUrl());
      from("kafka:" + kafkaTopic + "?brokers=" + config.kafkaClusterUrl() +
          (config.kafkaGroup().isPresent() ?
              "&groupId=" + config.kafkaGroup().get() : ""))
          .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
          .to("seda:metadata");
      from("seda:metadata")
          .bean(redisService, "process");
     }
    // @formatter:on

    // Display a warning if no Kafka topics are configured.
    if (config.kafkaTelemetryTopic().isEmpty() && config.kafkaMetadataTopic()
        .isEmpty()) {
      log.warn("No Kafka topics are configured.");
    } else {
      if (config.redisTtl() > 0) {
        log.info("Setting Redis TTL to '{}' minutes.", config.redisTtl());
      }
      log.info("All routes configured successfully, maximum value size for "
          + "caching is '{}' bytes.", config.redisMaxSize());
    }
  }
}
