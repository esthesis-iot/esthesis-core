package esthesis.dataflows.pingupdater.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflow.common.AvroUtils;
import esthesis.dataflows.pingupdater.config.AppConfig;
import esthesis.dataflows.pingupdater.service.PingService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.model.dataformat.AvroDataFormat;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class PingRoute extends RouteBuilder {

  @Inject
  PingService pingService;

  @Inject
  AppConfig config;

  @Inject
  AvroUtils avroUtils;

  @ConfigProperty(name = "quarkus.mongodb.connection-string")
  String mongoUrl;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-ping-updater");

    // Configure concurrency.
    ComponentsBuilderFactory.seda()
        .queueSize(config.queueSize())
        .defaultPollTimeout(config.pollTimeout())
        .concurrentConsumers(config.consumers())
        .register(getContext(), "seda");

    // @formatter:off
    log.info("Creating route from Kafka topic '{}' to MongoDB '{}' database "
            + "'{}'.",
        config.kafkaPingTopic(), mongoUrl, config.esthesisDbName());

    from("kafka:" + config.kafkaPingTopic() +
        "?brokers=" + config.kafkaClusterUrl() +
        (config.kafkaGroup().isPresent() ?
        "&groupId=" + config.kafkaGroup().get() : ""))
        .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
        .to("seda:ping");

    from("seda:ping")
        .bean(pingService, "searchForExistingDevice")
        .bean(pingService, "updateTimestamp")
        .to("mongodb:camelMongoClient?"
            + "database=" + config.esthesisDbName()
            + "&collection=Device"
            + "&operation=update");
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
