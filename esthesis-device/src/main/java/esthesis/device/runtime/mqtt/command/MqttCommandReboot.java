package esthesis.device.runtime.mqtt.command;

import esthesis.common.config.AppConstants.MqttCommand;
import esthesis.common.device.control.MqttControlCommand;
import esthesis.device.runtime.config.AppProperties;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.logging.Level;

@Service
@Validated
@Log
public class MqttCommandReboot {
  private final AppProperties appProperties;
  private final MqttCommandUtil mqttCommandUtil;

  public MqttCommandReboot(AppProperties appProperties,
    MqttCommandUtil mqttCommandUtil) {
    this.appProperties = appProperties;
    this.mqttCommandUtil = mqttCommandUtil;
  }

  @EventListener
  public void receiveCommand(MqttControlCommand cmd) throws IOException {
    if (cmd.getCommand() != MqttCommand.REBOOT || !mqttCommandUtil.isCommandSupported(cmd)) {
      return;
    }
    log.log(Level.FINE, "Processing command: {0}", cmd.getCommand());

    Runtime.getRuntime().exec(appProperties.getRebootCommand().split(" "));
  }
}
