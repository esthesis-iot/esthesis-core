package esthesis.device.runtime.mqtt.command;

import esthesis.common.device.control.MqttControlCommand;
import esthesis.device.runtime.config.AppProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class MqttCommandUtil {

  private final AppProperties appProperties;

  public MqttCommandUtil(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  public boolean isCommandSupported(MqttControlCommand mqttControlCommand) {
    return Arrays.asList(appProperties.getSupportedCommands().split(","))
      .contains(mqttControlCommand.getCommand().toString());
  }
}
