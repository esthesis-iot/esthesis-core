package esthesis.device.runtime.proxy.mqtt;

import esthesis.device.runtime.mqtt.MqttClient;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class MqttProxyInterceptor extends AbstractInterceptHandler {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(MqttProxyInterceptor.class.getName());
  private final MqttClient mqttProxyClient;

  public MqttProxyInterceptor(MqttClient mqttProxyClient) {
    this.mqttProxyClient = mqttProxyClient;
  }

  @Override
  public String getID() {
    return this.getClass().getName();
  }

  @Override
  public void onPublish(InterceptPublishMessage message) {
    byte[] bytes = new byte[message.getPayload().readableBytes()];
    message.getPayload().readBytes(bytes);
    mqttProxyClient.publish(message.getTopicName(), bytes, message.getQos().value(),
      message.isRetainFlag());
  }
}
