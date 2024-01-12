package esthesis.dataflows.rediscache.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.dataflows.rediscache.config.AppConfig;
import esthesis.dataflows.rediscache.service.RedisService;
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
public class RedisRoute extends RouteBuilder {

	@Inject
	RedisService redisService;

	@Inject
	AppConfig config;

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	@Override
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
    config.kafkaTelemetryTopic().ifPresent(val -> {
      //TODO redact password
      log.info("Setting up route from Kafka topic '{}' to Redis '{}'.", val, config.redisUrl());
      from("kafka:" + val)
          .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
          .to("seda:telemetry");
      from("seda:telemetry")
          .bean(redisService, "process");
    });

    config.kafkaMetadataTopic().ifPresent(val -> {
			//TODO redact password
      log.info("Setting up route from Kafka topic '{}' to Redis '{}'.",
          val, config.redisUrl());
      from("kafka:" + val)
          .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
          .to("seda:metadata");
      from("seda:metadata")
          .bean(redisService, "process");
    });
    // @formatter:on

		// Display a warning if no Kafka topics are configured.
		if (config.kafkaTelemetryTopic().isEmpty() && config.kafkaMetadataTopic()
			.isEmpty()) {
			log.warn("No Kafka topics are configured.");
		} else {
			if (config.redisTtl() > 0) {
				log.info("Setting Redis TTL to '{}' minutes.", config.redisTtl());
			}
			log.info("All routes configured successfully, maximum value size for "
				+ "caching is '{}' bytes.", config.redisMaxSize());
		}
	}
}
