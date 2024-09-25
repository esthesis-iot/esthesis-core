package esthesis.dataflows.commandreplyupdater.routes;

import esthesis.avro.util.camel.EsthesisCommandReplyDataFormat;
import esthesis.core.common.banner.BannerUtil;
import esthesis.dataflows.commandreplyupdater.config.AppConfig;
import esthesis.dataflows.commandreplyupdater.service.CommandReplyUpdaterService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.builder.component.dsl.KafkaComponentBuilderFactory.KafkaComponentBuilder;
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
			ComponentsBuilderFactory.kafka()
				.valueDeserializer(
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
    log.info("Creating route from Kafka topic '{}' to MongoDB '{}' database "
            + "'{}'.", config.kafkaCommandReplyTopic(), mongoUrl, config.esthesisDbName());

    from("kafka:" + config.kafkaCommandReplyTopic())
			.unmarshal(EsthesisCommandReplyDataFormat.create())
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
