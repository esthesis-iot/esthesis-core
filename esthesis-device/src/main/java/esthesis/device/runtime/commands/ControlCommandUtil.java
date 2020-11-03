package esthesis.device.runtime.commands;

import esthesis.common.device.control.ControlCommandRequest;
import esthesis.device.runtime.config.AppProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ControlCommandUtil {

  private final AppProperties appProperties;

  public ControlCommandUtil(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  public boolean isCommandEnabled(ControlCommandRequest mqttControlCommand) {
    return Arrays.asList(appProperties.getSupportedCommands().split(","))
      .contains(mqttControlCommand.getOperation());
  }
}
