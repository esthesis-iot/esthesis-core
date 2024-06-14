package esthesis.dataflows.pingupdater.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflow.common.EsthesisAvroFormats;
import esthesis.dataflows.pingupdater.config.AppConfig;
import esthesis.dataflows.pingupdater.service.PingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.builder.component.dsl.KafkaComponentBuilderFactory.KafkaComponentBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class PingRoute extends RouteBuilder {

	@Inject
	PingService pingService;

	@Inject
	AppConfig config;

	@ConfigProperty(name = "quarkus.mongodb.connection-string")
	String mongoUrl;

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	@Override
	@SuppressWarnings("java:S2629")
	public void configure() {
		BannerUtil.showBanner(appName);

		// Configure concurrency.
		ComponentsBuilderFactory.seda()
			.queueSize(config.concurrencyQueueSize())
			.defaultPollTimeout(config.concurrencyPollTimeout())
			.concurrentConsumers(config.concurrencyConsumers())
			.register(getContext(), "seda");

		// Configure Kafka.
		KafkaComponentBuilder kafkaComponentBuilder =
			ComponentsBuilderFactory.kafka().valueDeserializer(
					"org.apache.kafka.common.serialization.ByteArrayDeserializer")
				.brokers(config.kafkaClusterUrl());
		config.kafkaConsumerGroup().ifPresentOrElse(val -> {
				log.info("Using Kafka consumer group '{}'.", val);
				kafkaComponentBuilder.groupId(val);
			},
			() -> log.warn(
				"Kafka consumer group is not set, having more than one pods running in parallel "
					+ "may have unexpected results."));
		config.kafkaSecurityProtocol().ifPresentOrElse(val -> {
				log.info("Using Kafka security protocol '{}'.", val);
				kafkaComponentBuilder.securityProtocol(val);
				config.kafkaSaslMechanism().ifPresent(
					saslMechanism -> {
						log.info("Using Kafka SASL mechanism '{}'.", saslMechanism);
						kafkaComponentBuilder.saslMechanism(saslMechanism);
					});
				config.kafkaJaasConfig().ifPresent(
					jaasConfig -> {
						log.debug("Using Kafka JAAS configuration '{}'.", jaasConfig);
						kafkaComponentBuilder.saslJaasConfig(jaasConfig);
					});
			},
			() -> log.warn(
				"Kafka security protocol is not set, no security protocol will be configured."));
		kafkaComponentBuilder.register(getContext(), "kafka");

		// @formatter:off
    log.info("Creating route from Kafka topic '{}' to MongoDB '{}' database '{}'.",
        config.kafkaPingTopic(), mongoUrl, config.esthesisDbName());

    from("kafka:" + config.kafkaPingTopic())
			.log(LoggingLevel.DEBUG, log, "Received message from Kafka '${body}'.")
			.unmarshal(EsthesisAvroFormats.esthesisDataMessageFormat())
			.to("seda:ping");

    from("seda:ping")
				.log(LoggingLevel.DEBUG, log, "Processing message '${body}'.")
        .bean(pingService, "searchForExistingDevice")
        .bean(pingService, "updateTimestamp")
				.log(LoggingLevel.DEBUG, log, "Message processed, sending to MongoDB.")
        .to("mongodb:camelMongoClient?"
            + "database=" + config.esthesisDbName()
            + "&collection=Device"
            + "&operation=update")
				.log(LoggingLevel.DEBUG, log, "Message sent to MongoDB.");
    // @formatter:on

		log.info("Routes created successfully.");
	}
}
