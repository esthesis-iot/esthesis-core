package esthesis.dataflows.mqttclient.routes;

import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

@Slf4j
@ApplicationScoped
public class MqttRoute extends RouteBuilder {

  @Override
  public void configure() {
    from(
        "paho:test"
            + "?brokerUrl={{esthesis.dataflows.mqttclient.mqtt.brokerUrl}}"
            + "&password={{esthesis.dataflows.mqttclient.mqtt.username}}"
            + "&userName={{esthesis.dataflows.mqttclient.mqtt.password}}")
        .to("kafka:test?brokers={{esthesis.dataflows.mqttclient.kafka.brokers");
  }
}
