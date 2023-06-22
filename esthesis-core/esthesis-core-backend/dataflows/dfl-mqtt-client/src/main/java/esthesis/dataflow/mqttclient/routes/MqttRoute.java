package esthesis.dataflow.mqttclient.routes;

import esthesis.common.banner.BannerUtil;
import esthesis.common.crypto.CryptoService;
import esthesis.dataflow.mqttclient.config.AppConfig;
import esthesis.dataflow.mqttclient.service.DflMqttClientService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.pem.util.PemUtils;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.paho.PahoConstants;
import org.apache.camel.model.dataformat.AvroDataFormat;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

@Slf4j
@ApplicationScoped
public class MqttRoute extends RouteBuilder {

	@Inject
	DflMqttClientService dflMqttClientService;

	@Inject
	AppConfig config;

	@Inject
	CryptoService cryptoService;

	@Override
	@SuppressWarnings({"java:S1192", "java:S1602"})
	public void configure()
	throws IOException, UnrecoverableKeyException, CertificateException, KeyStoreException,
				 NoSuchAlgorithmException, InvalidKeySpecException, KeyManagementException,
				 NoSuchProviderException {
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
		Security.insertProviderAt(new BouncyCastleJsseProvider(), 2);
		BannerUtil.showBanner("dfl-mqtt-client");

		// Configure Kafka.
		ComponentsBuilderFactory.kafka()
			.brokers(config.kafkaClusterUrl())
			.valueDeserializer("org.apache.kafka.common.serialization.ByteArrayDeserializer")
			.valueSerializer("org.apache.kafka.common.serialization.ByteArraySerializer")
			.register(getContext(), "kafka");

		// Configure Paho, with SSL if needed.
		log.debug("Using MQTT broker URL '{}'.", config.mqttBrokerClusterUrl());
		ComponentsBuilderFactory.paho()
			.brokerUrl(config.mqttBrokerClusterUrl())
			.register(getContext(), "paho");
		String socketFactory;
		if (config.mqttBrokerClusterUrl().startsWith("ssl://")) {
			String caCert = null;
			String clientCert = null;
			String clientKey = null;
			if (StringUtils.isEmpty(config.mqttBrokerCert())) {
				log.warn("SSL is enabled but no client certificate is provided.");
			} else {
				clientCert = new String(Files.readAllBytes(Paths.get(config.mqttBrokerCert())));
				log.debug("Using client certificate '{}'.", clientCert);
			}
			if (StringUtils.isEmpty(config.mqttBrokerKey())) {
				log.warn("SSL is enabled but no client private key is provided.");
			} else {
				clientKey = new String(Files.readAllBytes(Paths.get(config.mqttBrokerKey())));
				log.debug("Using client private key '{}'.", clientKey);
			}
			if (StringUtils.isEmpty(config.mqttBrokerCa())) {
				log.warn("SSL is enabled but no CA certificate is provided.");
			} else {
				caCert = new String(Files.readAllBytes(Paths.get(config.mqttBrokerCa())));
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
    config.mqttTopicTelemetry().ifPresentOrElse(mqttTopic -> {
      config.kafkaTopicTelemetry().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.", mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#?" + socketFactory)
            .bean(dflMqttClientService, "toEsthesisDataMessages")
            .split(body())
            .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
            .toD("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka telemetry topic is not set."));
    }, () -> log.debug("MQTT telemetry topic is not set."));

    config.mqttTopicMetadata().ifPresentOrElse(mqttTopic -> {
      config.kafkaTopicMetadata().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.", mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#?" + socketFactory)
            .bean(dflMqttClientService, "toEsthesisDataMessages")
            .split(body())
            .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
            .toD("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka metadata topic is not set."));
    }, () -> log.debug("MQTT metadata topic is not set."));

    config.mqttTopicPing().ifPresentOrElse(mqttTopic -> {
      config.kafkaTopicPing().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.", mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#?" + socketFactory)
            .bean(dflMqttClientService, "toEsthesisDataMessages")
            .split(body())
            .marshal(new AvroDataFormat("esthesis.avro.EsthesisDataMessage"))
            .to("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka ping topic is not set."));
    }, () -> log.debug("MQTT ping topic is not set."));

    config.mqttTopicCommandReply().ifPresentOrElse(mqttTopic -> {
      config.kafkaTopicCommandReply().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from MQTT topic '{}' to Kafka topic '{}'.", mqttTopic, kafkaTopic);
        from("paho:" + mqttTopic + "/#?"  + socketFactory)
            .bean(dflMqttClientService, "processCommandReplyMessage")
            .marshal(new AvroDataFormat("esthesis.avro.EsthesisCommandReplyMessage"))
            .toD("kafka:" + kafkaTopic);
      }, () -> log.debug("Kafka command reply topic is not set."));
    }, () -> log.debug("MQTT command reply topic is not set."));

    config.mqttTopicCommandRequest().ifPresentOrElse(mqttTopic -> {
      config.kafkaTopicCommandRequest().ifPresentOrElse(kafkaTopic -> {
        log.info("Creating route from Kafka topic '{}' to MQTT topic '{}'.", kafkaTopic, mqttTopic);
        from("kafka:" + kafkaTopic)
            .setHeader(PahoConstants.CAMEL_PAHO_OVERRIDE_TOPIC,
                constant(mqttTopic).append("/").append(header(KafkaConstants.KEY)))
            .unmarshal(new AvroDataFormat("esthesis.avro.EsthesisCommandRequestMessage"))
            .log(LoggingLevel.DEBUG, log, "Received command request message '${body}'.")
            .bean(dflMqttClientService, "commandRequestToLineProtocol")
            .log(LoggingLevel.DEBUG, log, "Sending command request message '${body}' via MQTT.")
            .to("paho:dynamic?brokerUrl=" + config.mqttBrokerClusterUrl() + "&" + socketFactory);
      }, () -> log.debug("Kafka command request topic is not set."));
    }, () -> log.debug("MQTT command request topic is not set."));
    // @formatter:on

		log.info("Routes created successfully.");
	}
}
