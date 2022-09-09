package esthesis.dataflows.mqttclient.routes;

import esthesis.dataflows.mqttclient.config.AppConfig;
import esthesis.dataflows.mqttclient.service.MqttMessagingService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

@Slf4j
@ApplicationScoped
public class MqttRoute extends RouteBuilder {

  @Inject
  MqttMessagingService mqttMessagingService;

  @Inject
  AppConfig config;

  @Override
  public void configure() {
    from("paho:esthesis/telemetry/#" + "?brokerUrl=" + config.mqttBrokerUrl())
        .bean(mqttMessagingService, "process")
        .split(body())
        .toD("kafka:${header." + MqttMessagingService.HEADER_KAFKA_TOPIC + "}"
            + "?brokers=" + config.kafkaBrokers());
  }
}
