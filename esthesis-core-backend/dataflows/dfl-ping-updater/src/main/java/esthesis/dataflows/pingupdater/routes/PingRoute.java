package esthesis.dataflows.pingupdater.routes;

import esthesis.avro.util.AvroUtils;
import esthesis.common.banner.BannerUtil;
import esthesis.dataflows.pingupdater.config.AppConfig;
import esthesis.dataflows.pingupdater.service.PingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.builder.component.dsl.KafkaComponentBuilderFactory.KafkaComponentBuilder;
import org.apache.camel.model.dataformat.AvroDataFormat;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class PingRoute extends RouteBuilder {

	@Inject
	PingService pingService;

	@Inject
	AppConfig config;

	@Inject
	AvroUtils avroUtils;

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
		config.kafkaConsumerGroup().ifPresentOrElse(val -> {
				log.info("Using Kafka consumer group '{}'.", val);
				kafkaComponentBuilder.groupId(val);
			},
			() -> log.warn(
				"Kafka consumer group is not set, having more than one pods running in parallel "
					+ "may have unexpected results."));
		kafkaComponentBuilder.register(getContext(), "kafka");

		// @formatter:off
    log.info("Creating route from Kafka topic '{}' to MongoDB '{}' database "
            + "'{}'.",
        config.kafkaPingTopic(), mongoUrl, config.esthesisDbName());

    from("kafka:" + config.kafkaPingTopic())
        .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
        .to("seda:ping");

    from("seda:ping")
        .bean(pingService, "searchForExistingDevice")
        .bean(pingService, "updateTimestamp")
        .to("mongodb:camelMongoClient?"
            + "database=" + config.esthesisDbName()
            + "&collection=Device"
            + "&operation=update");
    // @formatter:on

		log.info("Routes created successfully.");
	}
}
