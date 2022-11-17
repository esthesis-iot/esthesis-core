package esthesis.dataflows.commandreplyupdater.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflows.commandreplyupdater.config.AppConfig;
import esthesis.dataflows.commandreplyupdater.service.CommandReplyUpdaterService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
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

    // @formatter:off
    log.info("Creating route from Kafka topic '{}' to MongoDB '{}' database "
            + "'{}'.",
        config.kafkaPingTopic(), mongoUrl, config.esthesisDbName());

    from("kafka:" + config.kafkaPingTopic() +
        "?brokers=" + config.kafkaClusterUrl() +
        (config.kafkaGroup().isPresent() ?
        "&groupId=" + config.kafkaGroup().get() : ""))
        .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisCommandReplyMessage"))
        .to("seda:commandReplyUpdater");

    from("seda:commandReplyUpdater")
        .bean(commandReplyUpdaterService, "createMongoEntity");
//        .to("mongodb:camelMongoClient?"
//            + "database=" + config.esthesisDbName()
//            + "&collection=CommandReply"
//            + "&operation=insert");
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
