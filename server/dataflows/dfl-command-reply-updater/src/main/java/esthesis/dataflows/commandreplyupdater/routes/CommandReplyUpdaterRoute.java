package esthesis.dataflows.commandreplyupdater.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflows.commandreplyupdater.config.AppConfig;
import esthesis.dataflows.commandreplyupdater.service.CommandReplyUpdaterService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.builder.component.dsl.KafkaComponentBuilderFactory.KafkaComponentBuilder;
import org.apache.camel.model.dataformat.AvroDataFormat;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class CommandReplyUpdaterRoute extends RouteBuilder {

  @Inject
  CommandReplyUpdaterService commandReplyUpdaterService;

  @Inject
  AppConfig config;

  @ConfigProperty(name = "quarkus.mongodb.connection-string")
  String mongoUrl;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-command-reply-updater");

    // Configure concurrency.
    ComponentsBuilderFactory.seda()
        .queueSize(config.queueSize())
        .defaultPollTimeout(config.pollTimeout())
        .concurrentConsumers(config.consumers())
        .register(getContext(), "seda");

    // Configure Kafka.
    KafkaComponentBuilder kafkaComponentBuilder =
        ComponentsBuilderFactory.kafka()
            .valueDeserializer(
                "org.apache.kafka.common.serialization.ByteArrayDeserializer")
            .brokers(config.kafkaClusterUrl());
    if (config.kafkaConsumerGroup().isPresent()) {
      log.info("Using Kafka consumer group '{}'.", config.kafkaConsumerGroup().get());
      kafkaComponentBuilder.groupId(config.kafkaConsumerGroup().get());
    } else {
      log.warn("Kafka consumer group is not set, having more than one pods running in parallel "
          + "may have unexpected results.");
    }
    kafkaComponentBuilder.register(getContext(), "kafka");

    // @formatter:off
    log.info("Creating route from Kafka topic '{}' to MongoDB '{}' database "
            + "'{}'.", config.kafkaCommandReplyTopic(), mongoUrl,
        config.esthesisDbName());

    from("kafka:" + config.kafkaCommandReplyTopic())
        .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisCommandReplyMessage"))
        .to("seda:commandReplyUpdater");

    from("seda:commandReplyUpdater")
        .bean(commandReplyUpdaterService, "createMongoEntity")
        .to("mongodb:camelMongoClient?"
            + "database=" + config.esthesisDbName()
            + "&collection=CommandReply"
            + "&operation=insert");
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
