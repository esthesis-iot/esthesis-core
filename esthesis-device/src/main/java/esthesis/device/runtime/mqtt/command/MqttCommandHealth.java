package esthesis.device.runtime.mqtt.command;

import esthesis.common.config.AppConstants.MqttCommand;
import esthesis.common.device.control.MqttControlCommand;
import esthesis.device.runtime.health.HealthMetadataCollector;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.logging.Level;

@Service
@Validated
@Log
public class MqttCommandHealth {
  private final HealthMetadataCollector healthMetadataCollector;
  private final MqttCommandUtil mqttCommandUtil;
  public MqttCommandHealth(
    HealthMetadataCollector healthMetadataCollector,
    MqttCommandUtil mqttCommandUtil) {
    this.healthMetadataCollector = healthMetadataCollector;
    this.mqttCommandUtil = mqttCommandUtil;
  }

  @EventListener
  public void receiveCommand(MqttControlCommand cmd) {
    if (cmd.getCommand() != MqttCommand.HEALTH || !mqttCommandUtil.isCommandSupported(cmd)) {
      return;
    }

    log.log(Level.FINE, "Processing command: {0}", cmd.getCommand());
    healthMetadataCollector.collectHealthData();
  }
}
