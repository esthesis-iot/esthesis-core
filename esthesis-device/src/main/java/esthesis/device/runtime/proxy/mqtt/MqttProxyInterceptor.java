package esthesis.device.runtime.proxy.mqtt;

import esthesis.device.runtime.config.AppConstants.Mqtt.EventType;
import esthesis.device.runtime.mqtt.MqttClient;
import esthesis.device.runtime.util.DeviceMessageUtil;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

@Log
@Component
public class MqttProxyInterceptor extends AbstractInterceptHandler {

  private final MqttClient mqttClient;
  private final DeviceMessageUtil deviceMessageUtil;

  public MqttProxyInterceptor(MqttClient mqttClient, DeviceMessageUtil deviceMessageUtil) {
    this.mqttClient = mqttClient;
    this.deviceMessageUtil = deviceMessageUtil;
  }

  @Override
  public String getID() {
    return this.getClass().getName();
  }

  @Override
  public void onPublish(InterceptPublishMessage message) {
    byte[] payload = new byte[message.getPayload().readableBytes()];
    message.getPayload().readBytes(payload);
    log.log(Level.FINEST, "Proxying to MQTT topic {0}: {1}",
      new String[]{message.getTopicName(), new String(payload)});
    mqttClient.publish(EventType.valueOf(message.getTopicName().toUpperCase()), payload,
      message.getQos().value(), message.isRetainFlag());
  }
}
