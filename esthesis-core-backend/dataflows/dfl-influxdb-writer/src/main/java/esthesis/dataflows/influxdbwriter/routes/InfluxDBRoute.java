package esthesis.dataflows.influxdbwriter.routes;

import esthesis.avro.util.camel.EsthesisDataMessageDataFormat;
import esthesis.core.common.banner.BannerUtil;
import esthesis.dataflows.influxdbwriter.config.AppConfig;
import esthesis.dataflows.influxdbwriter.service.InfluxDBService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.builder.component.dsl.KafkaComponentBuilderFactory.KafkaComponentBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class InfluxDBRoute extends RouteBuilder {

	@Inject
	InfluxDBService influxDBService;

	@Inject
	AppConfig config;

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	@Override
	public void configure() {
		BannerUtil.showBanner(appName);

		// Configure concurrency.
		ComponentsBuilderFactory.seda().queueSize(config.concurrencyQueueSize())
			.defaultPollTimeout(config.concurrencyPollTimeout())
			.concurrentConsumers(config.concurrencyConsumers()).register(getContext(), "seda");

		// Configure Kafka.
		KafkaComponentBuilder kafkaComponentBuilder = ComponentsBuilderFactory.kafka()
			.valueDeserializer("org.apache.kafka.common.serialization.ByteArrayDeserializer")
			.brokers(config.kafkaClusterUrl());
		config.kafkaConsumerGroup().ifPresentOrElse(val -> {
			log.info("Using Kafka consumer group '{}'.", val);
			kafkaComponentBuilder.groupId(val);
		}, () -> log.warn(
			"Kafka consumer group is not set, having more than one pods running in parallel "
				+ "may have unexpected results."));
		config.kafkaSecurityProtocol().ifPresentOrElse(val -> {
			log.info("Using Kafka security protocol '{}'.", val);
			kafkaComponentBuilder.securityProtocol(val);
			config.kafkaSaslMechanism().ifPresent(saslMechanism -> {
				log.info("Using Kafka SASL mechanism '{}'.", saslMechanism);
				kafkaComponentBuilder.saslMechanism(saslMechanism);
			});
			config.kafkaJaasConfig().ifPresent(jaasConfig -> {
				log.debug("Using Kafka JAAS configuration '{}'.", jaasConfig);
				kafkaComponentBuilder.saslJaasConfig(jaasConfig);
			});
		}, () -> log.warn(
			"Kafka security protocol is not set, no security protocol will be configured."));
		kafkaComponentBuilder.register(getContext(), "kafka");

		// @formatter:off
    config.kafkaTelemetryTopic().ifPresentOrElse(val -> {
      log.info("Setting up route from Kafka topic '{}' to InfluxDB '{}' "
          + "bucket '{}'.", val, config.influxUrl(), config.influxBucket());
      from("kafka:" + val)
				.unmarshal(EsthesisDataMessageDataFormat.create())
				.to("seda:telemetry");
      from("seda:telemetry")
          .bean(influxDBService, "process");
    }, () -> log.debug("Kafka telemetry topic is not set, skipping route."));


    config.kafkaMetadataTopic().ifPresentOrElse(val -> {
      log.info("Setting up route from Kafka topic '{}' to InfluxDB '{}' "
          + "bucket '{}'.", val, config.influxUrl(), config.influxBucket());
      from("kafka:" + val)
				.unmarshal(EsthesisDataMessageDataFormat.create())
				.to("seda:metadata");
      from("seda:metadata")
          .bean(influxDBService, "process");
    }, () -> log.debug("Kafka metadata topic is not set, skipping route."));
    // @formatter:on

		// Display a warning if no Kafka topics are configured.
		if (config.kafkaTelemetryTopic().isEmpty() && config.kafkaMetadataTopic().isEmpty()) {
			log.warn("No Kafka topics are configured.");
		}
	}
}
