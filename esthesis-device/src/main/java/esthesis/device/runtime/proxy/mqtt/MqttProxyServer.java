package esthesis.device.runtime.proxy.mqtt;

import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.mqtt.MqttClient;
import io.moquette.BrokerConstants;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MqttProxyServer {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(MqttProxyServer.class.getName());

  private io.moquette.broker.Server server;
  private final AppProperties appProperties;
  private final MqttClient mqttProxyClient;

  public MqttProxyServer(AppProperties appProperties, MqttClient mqttProxyClient) {
    this.appProperties = appProperties;
    this.mqttProxyClient = mqttProxyClient;
  }

  public void start() {
    try {
      server = new io.moquette.broker.Server();
      Properties properties = new Properties();
      properties.put(BrokerConstants.PORT_PROPERTY_NAME,
        String.valueOf(appProperties.getProxyMqttPort()));
      server.startServer(properties);
      server.addInterceptHandler(new MqttProxyInterceptor(mqttProxyClient));
      LOGGER.log(Level.CONFIG, "Embedded MQTT server started on port {0}.",
        String.valueOf(appProperties.getProxyMqttPort()));
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not start embedded MQTT server.", e);
    }
  }

  @PreDestroy
  public void stop() {
    if (server != null) {
      mqttProxyClient.disconnect();
      server.stopServer();
      LOGGER.log(Level.CONFIG, "Stopped embedded MQTT server on port {0}.",
        String.valueOf(appProperties.getProxyMqttPort()));
    }
  }
}
