package esthesis.dataflow.mqttclient.routes;

import esthesis.avro.util.camel.EsthesisCommandReplyDataFormat;
import esthesis.avro.util.camel.EsthesisCommandRequestDataFormat;
import esthesis.avro.util.camel.EsthesisDataMessageDataFormat;
import esthesis.common.banner.BannerUtil;
import esthesis.dataflow.mqttclient.config.AppConfig;
import esthesis.dataflow.mqttclient.service.DflMqttClientService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.pem.util.PemUtils;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.builder.component.dsl.KafkaComponentBuilderFactory.KafkaComponentBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.paho.PahoConstants;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class MqttRoute extends RouteBuilder {

	@Inject
	DflMqttClientService dflMqttClientService;

	@Inject
	AppConfig config;

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	@Override
	@SuppressWarnings({"java:S1192", "java:S1602"})
	public void configure()
	throws IOException {
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
		Security.insertProviderAt(new BouncyCastleJsseProvider(), 2);
		BannerUtil.showBanner(appName);

		// Configure Kafka.
		KafkaComponentBuilder kafkaComponentBuilder =
			ComponentsBuilderFactory.kafka()
				.valueDeserializer("org.apache.kafka.common.serialization.ByteArrayDeserializer")
				.valueSerializer("org.apache.kafka.common.serialization.ByteArraySerializer")
				.brokers(config.kafkaClusterUrl());
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

		// Configure Paho, with SSL if needed.
		log.info("Using MQTT broker URL '{}'.", config.mqttBrokerClusterUrl());
		ComponentsBuilderFactory.paho()
			.brokerUrl(config.mqttBrokerClusterUrl())
			.keepAliveInterval(config.mqttBrokerKeepAliveInterval())
			.register(getContext(), "paho");
		String socketFactory;
		if (config.mqttBrokerClusterUrl().startsWith("ssl://")) {
			String caCert = null;
			String clientCert = null;
			String clientKey = null;
			if (config.mqttBrokerCert().isEmpty()) {
				log.warn("SSL is enabled but no client certificate is provided.");
			} else {
				clientCert = Files.readString(Paths.get(config.mqttBrokerCert().get()));
				log.debug("Using client certificate '{}'.", clientCert);
			}
			if (config.mqttBrokerKey().isEmpty()) {
				log.warn("SSL is enabled but no client private key is provided.");
			} else {
				clientKey = Files.readString(Paths.get(config.mqttBrokerKey().get()));
				log.debug("Using client private key '{}'.", clientKey);
			}
			if (config.mqttBrokerCa().isEmpty()) {
				log.warn("SSL is enabled but no CA certificate is provided.");
			} else {
				caCert = Files.readString(Paths.get(config.mqttBrokerCa().get()));
				log.debug("Using CA certificate '{}'.", caCert);
			}

			X509ExtendedKeyManager keyManager = PemUtils.parseIdentityMaterial(clientCert, clientKey,
				null);
			X509ExtendedTrustManager trustManager = PemUtils.parseTrustMaterial(caCert);
			getContext().getRegistry().bind("sslSocketFactory",
				SSLFactory.builder()
					.withIdentityMaterial(keyManager)
					.withTrustMaterial(trustManager)
					.build().getSslSocketFactory());
			socketFactory = "socketFactory=#sslSocketFactory";
		} else {
			socketFactory = "";
		}

		// @formatter:off
    config.mqttTelemetryTopic().ifPresentOrElse(mqttTopic -> {
      config.kafkaTelemetryTopic().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.", mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#?" + socketFactory)
					.routeId("mqtt-telemetry-to-kafka")
					.bean(dflMqttClientService, "toEsthesisDataMessages")
					.split(body())
					.marshal(EsthesisDataMessageDataFormat.create())
					.log(LoggingLevel.DEBUG, log, "Sending telemetry message '${body}'.")
					.toD("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka telemetry topic is not set."));
    }, () -> log.debug("MQTT telemetry topic is not set."));

    config.mqttMetadataTopic().ifPresentOrElse(mqttTopic -> {
      config.kafkaMetadataTopic().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.", mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#?" + socketFactory)
					.routeId("mqtt-metadata-to-kafka")
					.bean(dflMqttClientService, "toEsthesisDataMessages")
					.split(body())
					.marshal(EsthesisDataMessageDataFormat.create())
					.log(LoggingLevel.DEBUG, log, "Sending metadata message '${body}'")
					.toD("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka metadata topic is not set."));
    }, () -> log.debug("MQTT metadata topic is not set."));

    config.mqttPingTopic().ifPresentOrElse(mqttTopic -> {
      config.kafkaPingTopic().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.", mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#?" + socketFactory)
					.routeId("mqtt-ping-to-kafka")
					.bean(dflMqttClientService, "toEsthesisDataMessages")
					.split(body())
					.marshal(EsthesisDataMessageDataFormat.create())
					.log(LoggingLevel.DEBUG, log, "Sending ping message '${body}'")
					.to("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka ping topic is not set."));
    }, () -> log.debug("MQTT ping topic is not set."));

    config.mqttCommandReplyTopic().ifPresentOrElse(mqttTopic -> {
      config.kafkaCommandReplyTopic().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.", mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#?"  + socketFactory)
					.routeId("mqtt-command-reply-to-kafka")
					.bean(dflMqttClientService, "processCommandReplyMessage")
					.marshal(EsthesisCommandReplyDataFormat.create())
					.log(LoggingLevel.DEBUG, log, "Sending command reply message '${body}'")
					.toD("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka command reply topic is not set."));
    }, () -> log.debug("MQTT command reply topic is not set."));

    config.mqttCommandRequestTopic().ifPresentOrElse(mqttTopic -> {
      config.kafkaCommandRequestTopic().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from Kafka topic '{}' to MQTT topic '{}'.", kafkaTopic, mqttTopic);
        from("kafka:" + kafkaTopic)
					.routeId("kafka-command-request-to-mqtt")
					.unmarshal(EsthesisCommandRequestDataFormat.create())
					.setHeader(PahoConstants.CAMEL_PAHO_OVERRIDE_TOPIC,
						constant(mqttTopic).append("/").append(header(KafkaConstants.KEY)))
					.log(LoggingLevel.DEBUG, log, "Received command request message '${body}'.")
					.bean(dflMqttClientService, "commandRequestToLineProtocol")
					.log(LoggingLevel.DEBUG, log, "Sending command request message '${body}'")
					.to("paho:dynamic?brokerUrl=" + config.mqttBrokerClusterUrl() +
						(StringUtils.isEmpty(socketFactory) ? "" : "&" + socketFactory));
      }, () -> log.debug("Kafka command request topic is not set."));
    }, () -> log.debug("MQTT command request topic is not set."));
    // @formatter:on

		log.info("Routes created successfully.");
	}
}
