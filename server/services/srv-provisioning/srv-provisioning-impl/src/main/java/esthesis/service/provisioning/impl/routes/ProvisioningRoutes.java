package esthesis.service.provisioning.impl.routes;

import esthesis.service.provisioning.impl.service.ConfigService;
import esthesis.service.provisioning.impl.service.ProvisioningService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class ProvisioningRoutes extends RouteBuilder {

  @Inject
  ProvisioningService provisioningService;

  @Inject
  ConfigService configService;

  @ConfigProperty(name = "quarkus.mongodb.database")
  String database;

  public static final String PROPERTY_PROVISIONING_PACKAGE_ID = "X-ProvisioningPackageId";

  @Override
  public void configure() throws Exception {
    // @formatter:off
    // Timer route.
//    from("timer:provisioning?period=10000")
//        .to("direct:cache");


    // Caches a specific provisioning package.
    // The body of the incoming message to this route should contain the provisioning package id.
    // Caching takes place irrespectively of the packages 'active' status.
    from("direct:cacheOne")
        .convertBodyTo(ObjectId.class)
        .setProperty(PROPERTY_PROVISIONING_PACKAGE_ID, simple("${body}"))
        .to("mongodb:camelMongoClient?"
            + "database=" + database
            + "&collection=ProvisioningPackage"
            + "&operation=findById")
        .to("direct:packageTypeRouting");

    // Find all provisioning packages requiring caching.
    // Caching takes place only for packages with 'active' status and 'cacheStatus = 0'.
    from("direct:cacheAll")
        .bean(provisioningService, "searchForPackagesToCache")
        .to("mongodb:camelMongoClient?"
            + "database=" + database
            + "&collection=ProvisioningPackage"
            + "&operation=findAll")
        .log(LoggingLevel.DEBUG, log, "Found '${body.size()}' provisioning packages to cache.")
        .split(body())
        .bean(provisioningService, "saveProvisioningPackageId")
        .to("direct:packageTypeRouting");

    // Route provisioning packages to the appropriate cache route based on the package type.
    from("direct:packageTypeRouting")
        .log(LoggingLevel.DEBUG, log, "Caching provisioning package '${body}'.")
        .choice()
          .when(simple("${body} contains 'type=FTP'"))
            .to("direct:ftp")
          .when(simple("${body} contains 'type=WEB'"))
            .to("direct:web");

    // FTP route.
    from("direct:ftp")
        .bean(configService, "setupFtpConfig")
        .choice()
          .when(simple("${header.username} != null"))
            .pollEnrich().simple(
                "ftp://${header.host}/${header.directory}?binary=true&username=${header.username}"
                    + "&password=${header.password}&fileName=${header.filename}&disconnect=true"
                    + "&passiveMode=${header.passive}")
        .endChoice()
        .otherwise()
          .pollEnrich().simple("ftp://${header.host}/${header.directory}?binary=true"
          + "&fileName=${header.filename}&disconnect=true&passiveMode=${header.passive}"
          )
        .endChoice().end()
//        .to("file:///Users/nassos/tmp/camel")
        .log(LoggingLevel.DEBUG, log, "Finished downloading provisioning package.")
        .to("direct:redis");

    // Web route.
    from("direct:web")
        .log(LoggingLevel.DEBUG, log, "WEB.");

    // Redis upload.
    from("direct:redis")
        .log(LoggingLevel.DEBUG, log, "Caching provisioning package to Redis.")
        .bean(provisioningService, "cacheInRedis")
        .log(LoggingLevel.DEBUG, log, "Finished caching provisioning package to Redis.");

  // @formatter:on
  }
}

