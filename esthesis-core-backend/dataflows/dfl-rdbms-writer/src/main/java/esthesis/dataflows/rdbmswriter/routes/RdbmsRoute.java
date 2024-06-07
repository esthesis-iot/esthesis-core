package esthesis.dataflows.rdbmswriter.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflows.rdbmswriter.config.AppConfig;
import esthesis.dataflows.rdbmswriter.service.RdbmsService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.builder.component.dsl.KafkaComponentBuilderFactory.KafkaComponentBuilder;
import org.apache.camel.model.dataformat.AvroLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class RdbmsRoute extends RouteBuilder {

	@Inject
	RdbmsService rdbmsService;

	@Inject
	AppConfig config;

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	@SuppressWarnings("java:S2629")
	private void printRouteInfo(String topic) {
		log.info("Setting up route from Kafka topic '{}' to '{}', storage "
				+ "strategy '{}'.",
			topic, config.dbKind() + " - " + config.dbJdbcUrl(),
			config.dbStorageStrategy() +
				(config.dbStorageStrategy() == AppConfig.STORAGE_STRATEGY.SINGLE
					? ", table: " + config.dbStorageStrategySingleTableName()
					: ""));
	}

	@Override
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
    config.kafkaTelemetryTopic().ifPresentOrElse(val -> {
      printRouteInfo(val);
      from("kafka:" + val)
				.unmarshal().avro(AvroLibrary.Jackson, "esthesis.avro.EsthesisDataMessage")
        .to("seda:telemetry");
      from("seda:telemetry")
        .bean(rdbmsService, "process");
    }, () -> log.debug("Kafka telemetry topic is not set, skipping route."));

    config.kafkaMetadataTopic().ifPresentOrElse(val -> {
      printRouteInfo(val);
      from("kafka:" + val)
				.unmarshal().avro(AvroLibrary.Jackson, "esthesis.avro.EsthesisDataMessage")
				.to("seda:metadata");
      from("seda:metadata")
          .bean(rdbmsService, "process");
    }, () -> log.debug("Kafka metadata topic is not set, skipping route."));

    // @formatter:on

		// Display a warning if no Kafka topics are configured.
		if (config.kafkaTelemetryTopic().isEmpty() && config.kafkaMetadataTopic()
			.isEmpty()) {
			log.warn("No Kafka topics are configured.");
		} else {
			log.info("All routes configured successfully.");
		}
	}
}
