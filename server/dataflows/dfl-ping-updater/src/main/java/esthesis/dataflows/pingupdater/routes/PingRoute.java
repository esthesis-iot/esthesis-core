package esthesis.dataflows.pingupdater.routes;

import com.mongodb.client.model.Filters;
import esthesis.common.banner.BannerUtil;
import esthesis.dataflow.common.DflUtils;
import esthesis.dataflows.pingupdater.config.AppConfig;
import esthesis.dataflows.pingupdater.service.PingService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.camel.model.dataformat.AvroDataFormat;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class PingRoute extends RouteBuilder {

  @Inject
  PingService pingService;

  @Inject
  DflUtils dflUtils;

  @Inject
  AppConfig config;

  @ConfigProperty(name = "quarkus.mongodb.connection-string")
  String mongoUrl;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-ping-updater");

    // @formatter:off
    log.info("Creating route from Kafka topic '{}' to MongoDB '{}' database "
            + "'{}'.",
        config.kafkaPingTopic(), mongoUrl, config.esthesisDbName());
    from("kafka:" + config.kafkaPingTopic() +
        "?brokers=" + config.kafkaClusterUrl() +
        (config.kafkaGroup().isPresent() ?
        "&groupId=" + config.kafkaGroup().get() : ""))
        .unmarshal(new AvroDataFormat("esthesis.dataflow.common.parser.EsthesisMessage"))
        .setHeader(MongoDbConstants.CRITERIA, new Expression() {
          @Override
          public <T> T evaluate(Exchange exchange, Class<T> type) {
            Bson equalsClause = Filters.eq("hardwareId",
                exchange.getIn().getHeader(KafkaConstants.KEY));
            return exchange.getContext().getTypeConverter().convertTo(type, equalsClause);
          }})
        .bean(pingService, "updateTimestamp")
        .to("mongodb:camelMongoClient?"
            + "database=" + config.esthesisDbName()
            + "&collection=Device"
            + "&operation=update");
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
