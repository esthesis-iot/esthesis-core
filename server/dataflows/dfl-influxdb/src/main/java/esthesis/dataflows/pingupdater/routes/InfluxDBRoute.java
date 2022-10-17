package esthesis.dataflows.pingupdater.routes;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import esthesis.common.banner.BannerUtil;
import esthesis.dataflows.pingupdater.config.AppConfig;
import esthesis.dataflows.pingupdater.service.PingService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

@Slf4j
@ApplicationScoped
public class InfluxDBRoute extends RouteBuilder {

  @Inject
  PingService pingService;

  @Inject
  AppConfig config;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-influxdb");

    InfluxDBClient influxDBClient = InfluxDBClientFactory
        .create("http://esthesis-influxdb:8086", "esthesis",
            "esthesis".toCharArray());

    System.out.println(
        influxDBClient.getBucketsApi().findBuckets()
    );

    // @formatter:off

//    from("kafka:" + config.kafkaPingTopic() +
//        "?brokers=" + config.kafkaClusterUrl() +
//        (config.kafkaGroup().isPresent() ?
//        "&groupId=" + config.kafkaGroup().get() : ""))
//        .bean(pingService, "extractPingTimestamp")
//        .setHeader(MongoDbConstants.CRITERIA, new Expression() {
//          @Override
//          public <T> T evaluate(Exchange exchange, Class<T> type) {
//            Bson equalsClause = Filters.eq("hardwareId",
//                exchange.getIn().getHeader("Kafka.KEY"));
//            return exchange.getContext().getTypeConverter().convertTo(type, equalsClause);
//          }})
//        .bean(pingService, "updateTimestamp")
//        .to("mongodb:camelMongoClient?"
//            + "database=" + config.esthesisDbName()
//            + "&collection=Device"
//            + "&operation=update");
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
