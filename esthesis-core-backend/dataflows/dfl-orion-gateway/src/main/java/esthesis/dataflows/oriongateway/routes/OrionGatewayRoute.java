package esthesis.dataflows.oriongateway.routes;

import esthesis.avro.util.camel.EsthesisDataMessageDataFormat;
import esthesis.common.banner.BannerUtil;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.dataflows.oriongateway.service.OrionClientService;
import esthesis.dataflows.oriongateway.service.OrionGatewayService;
import esthesis.dataflows.oriongateway.service.OrionMessagingService;
import esthesis.util.kafka.notifications.common.AppMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.builder.component.dsl.KafkaComponentBuilderFactory.KafkaComponentBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class OrionGatewayRoute extends RouteBuilder {

	@Inject
	OrionMessagingService orionMessagingService;

	@Inject
	OrionGatewayService orionGatewayService;

	@Inject
	OrionClientService orionClientService;

	@Inject
	AppConfig config;

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	@Override
	@SuppressWarnings("java:S2629")
	public void configure() {
		BannerUtil.showBanner(appName);

		// Check if existing devices should be registered in Orion.
		if (config.orionRetroCreateDevicesOnSchedule()) {
			log.info("Will be adding existing devices to Orion on schedule '{}'.",
				config.orionRetroCreateDevicesSchedule());
		}

		if (config.orionRetroCreateDevicesOnBoot()) {
			log.info("Adding existing devices to Orion on boot.");
			orionGatewayService.addExistingEsthesisDevicesToOrion();
		}

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

		// Try to establish a connection to the Orion server.
		log.info("Connecting to Orion server at '{}', version '{}'.", config.orionUrl(),
			orionClientService.getVersion());

		// Listen for application messages.
		// @formatter:off
    if (config.kafkaApplicationTopic().isPresent()) {
      log.info("Listening for application messages on Kafka topic '{}'.",
          config.kafkaApplicationTopic().get());

      from("kafka:" + config.kafkaApplicationTopic().get())
          .unmarshal().json(AppMessage.class)
          .to("seda:appMessage");
      from("seda:appMessage")
          .bean(orionMessagingService, "onAppMessage");
    } else {
      log.warn("No Kafka topic for application messages is set.");
    }
    // @formatter:on

		// Listen for telemetry messages.
		// @formatter:off
    if (config.kafkaTelemetryTopic().isPresent()) {
      log.info("Creating route from Kafka topic '{}'.", config.kafkaTelemetryTopic().get());

      from("kafka:" + config.kafkaTelemetryTopic().get())
				.unmarshal(EsthesisDataMessageDataFormat.create())
				.to("seda:telemetry");
      from("seda:telemetry")
          .bean(orionGatewayService, "processData");
      } else {
        log.warn("No Kafka topic for telemetry messages is set.");
    }
    // @formatter:on

		// Listen for metadata messages.
		// @formatter:off
    if (config.kafkaMetadataTopic().isPresent()) {
      log.info("Creating route from Kafka topic '{}'.", config.kafkaMetadataTopic().get());

      from("kafka:" + config.kafkaMetadataTopic().get())
				.unmarshal(EsthesisDataMessageDataFormat.create())
				.to("seda:metadata");
      from("seda:metadata")
          .bean(orionGatewayService, "processData");
      } else {
        log.warn("No Kafka topic for metadata messages is set.");
    }
    // @formatter:on

		log.info("Routes created successfully.");
	}
}
