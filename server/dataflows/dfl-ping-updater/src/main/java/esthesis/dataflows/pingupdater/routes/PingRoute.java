package esthesis.dataflows.pingupdater.routes;

import com.mongodb.client.model.Filters;
import esthesis.common.banner.BannerUtil;
import esthesis.dataflows.pingupdater.service.PingService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.bson.conversions.Bson;

@Slf4j
@ApplicationScoped
public class PingRoute extends RouteBuilder {

  @Inject
  PingService pingService;

//  @Inject
//  AppConfig config;

  @Override
  public void configure() {
    BannerUtil.showBanner("dfl-ping-updater");

    // @formatter:off
    from("kafka:esthesis-ping?brokers=esthesis-dev-kafka:9094&groupId"
        + "=esthesis-ping-updater")
        .bean(pingService, "extractPingTimestamp")
        .setHeader(MongoDbConstants.CRITERIA, new Expression() {
          @Override
          public <T> T evaluate(Exchange exchange, Class<T> type) {
            Bson equalsClause = Filters.eq("hardwareId",
                exchange.getIn().getHeader("Kafka.KEY"));
            return exchange.getContext().getTypeConverter().convertTo(type, equalsClause);
          }})
        .bean(pingService, "updateTimestamp")
        .to("mongodb:camelMongoClient?"
            + "database=esthesis"
            + "&collection=Device"
            + "&operation=update");
    // @formatter:on

    log.info("Routes created successfully.");
  }
}
