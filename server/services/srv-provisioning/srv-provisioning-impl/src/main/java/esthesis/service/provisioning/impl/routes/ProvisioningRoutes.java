package esthesis.service.provisioning.impl.routes;

import esthesis.common.AppConstants.Provisioning.Type;
import esthesis.service.provisioning.impl.service.ConfigService;
import esthesis.service.provisioning.impl.service.ProvisioningRoutingService;
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
  ProvisioningRoutingService provisioningRoutingService;

  @Inject
  ConfigService configService;

  @ConfigProperty(name = "quarkus.mongodb.database")
  String database;

  public static final String PROPERTY_PROVISIONING_PACKAGE_ID = "X-ProvisioningPackageId";
  public static final String PROPERTY_PROVISIONING_PACKAGE_SIZE = "X-ProvisioningPackageSize";
  public static final String PROPERTY_PROVISIONING_PACKAGE_HASH = "X-ProvisioningPackageHash";
  public static final String PROPERTY_PROVISIONING_PACKAGE_TYPE = "X-ProvisioningPackageType";
  public static final String PROPERTY_EXCEPTION_MESSAGE = "X-ProvisioningPackageExceptionMessage";

  @Override
  public void configure() {

    // A generic exception handler.
    onException(Exception.class)
        .setProperty(PROPERTY_EXCEPTION_MESSAGE, simple("${exception.message}"))
        .to("direct:fail");

    // @formatter:off

    // Caches a specific provisioning package.
    // The body of the incoming message to this route should contain the provisioning package id.
    // Caching takes place irrespectively of the package's 'active' status.
    from("direct:cacheOne")
      .convertBodyTo(ObjectId.class)
      .to("mongodb:camelMongoClient?"
          + "database=" + database
          + "&collection=ProvisioningPackage"
          + "&operation=findById")
      .to("direct:packageTypeRouting");

    // Attempts to recache all active packages, irrespectively of cache status.
    from("direct:cacheAll")
      .bean(provisioningRoutingService, "searchAllActive")
      .to("mongodb:camelMongoClient?"
          + "database=" + database
          + "&collection=ProvisioningPackage"
          + "&operation=findAll")
      .split(body())
      .to("direct:packageTypeRouting");

    // Route provisioning packages to the appropriate cache route based on the package type.
    from("direct:packageTypeRouting")
        .log(LoggingLevel.DEBUG, log, "Caching provisioning package '${body}'.")
        .bean(provisioningRoutingService, "extractProvisioningPackageInfo")
        .log(LoggingLevel.DEBUG, log, "Setting cache status to 'In progress'.")
        .bean(provisioningRoutingService, "setCacheStatusToInProgress")
        .to("mongodb:camelMongoClient?"
            + "database=" + database
            + "&collection=ProvisioningPackage"
            + "&operation=update")
        .setBody(simple("${exchangeProperty." + PROPERTY_PROVISIONING_PACKAGE_ID + "}"))
        .to("mongodb:camelMongoClient?"
            + "database=" + database
            + "&collection=ProvisioningPackage"
            + "&operation=findById")
        .log(LoggingLevel.DEBUG, log, "Deciding package type route.")
        .choice()
          .when(exchangeProperty(PROPERTY_PROVISIONING_PACKAGE_TYPE).isEqualTo(Type.FTP))
            .log(LoggingLevel.DEBUG, log, "FTP package type.")
            .to("direct:ftp")
          .when(exchangeProperty(PROPERTY_PROVISIONING_PACKAGE_TYPE).isEqualTo(Type.WEB))
            .log(LoggingLevel.DEBUG, log, "WEB package type.")
            .to("direct:web")
          .otherwise()
            .log(LoggingLevel.ERROR, log, "Could not determine package type for package ''${body}''.")
            .to("direct:fail");

    // FTP route.
    from("direct:ftp")
        .bean(configService, "setupFtpConfig")
        .choice()
          .when(simple("${header.username} != null"))
            .pollEnrich().simple(
                "ftp://${header.host}/${header.directory}?binary=true&username=${header.username}"
                    + "&password=${header.password}&fileName=${header.filename}&disconnect=true"
                    + "&passiveMode=${header.passive}&throwExceptionOnConnectFailed=true")
        .endChoice()
        .otherwise()
          .pollEnrich().simple("ftp://${header.host}/${header.directory}?binary=true"
            + "&fileName=${header.filename}&disconnect=true&passiveMode=${header.passive}"
            + "&throwExceptionOnConnectFailed=true")
        .endChoice().end()
        .log(LoggingLevel.DEBUG, log, "Finished downloading provisioning package.")
        .to("direct:redis");

    // Web route.
    from("direct:web")
        .log(LoggingLevel.DEBUG, log, "WEB.");

    // Redis upload.
    from("direct:redis")
        .log(LoggingLevel.DEBUG, log, "Caching provisioning package to Redis.")
        .bean(provisioningRoutingService, "checkHash")
        .bean(provisioningRoutingService, "cacheInRedis")
        .log(LoggingLevel.DEBUG, log, "Finished caching provisioning package to Redis.")
        .to("direct:success");

    // Generic exception handler:
    // - It updates the package as CacheStatus.FAILED.
    // - Sets the package logging to the underlying exception.
    from("direct:fail")
        .bean(provisioningRoutingService, "setFailureConditions")
        .to("mongodb:camelMongoClient?"
            + "database=" + database
            + "&collection=ProvisioningPackage"
            + "&operation=update")
        .log(LoggingLevel.DEBUG, log, "Caching finished with error.");

    // The final route when a caching operation was successful:
    // - It updates the package with CacheStatus.SUCCESS.
    // - Clears any previous log for that package.
    from("direct:success")
        .bean(provisioningRoutingService, "setSuccessConditions")
        .to("mongodb:camelMongoClient?"
            + "database=" + database
            + "&collection=ProvisioningPackage"
            + "&operation=update")
        .log(LoggingLevel.DEBUG, log, "Caching finished successfully.");

  // @formatter:on
  }
}

