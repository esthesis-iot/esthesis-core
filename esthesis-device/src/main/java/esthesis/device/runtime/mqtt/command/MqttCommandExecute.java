package esthesis.device.runtime.mqtt.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.config.AppConstants.Mqtt.EventType;
import esthesis.common.config.AppConstants.MqttCommand;
import esthesis.common.device.control.MqttCommandReplyPayload;
import esthesis.common.device.control.MqttControlCommand;
import esthesis.device.runtime.mqtt.MqttClient;
import lombok.extern.java.Log;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@Service
@Validated
@Log
public class MqttCommandExecute {

  private final MqttCommandUtil mqttCommandUtil;
  private final MqttClient mqttClient;
  private final ObjectMapper objectMapper;

  public MqttCommandExecute(MqttCommandUtil mqttCommandUtil,
    MqttClient mqttClient, ObjectMapper objectMapper) {
    this.mqttCommandUtil = mqttCommandUtil;
    this.mqttClient = mqttClient;
    this.objectMapper = objectMapper;
  }

  @EventListener
  public void receiveCommand(MqttControlCommand cmd) {
    if (cmd.getCommand() != MqttCommand.EXECUTE || !mqttCommandUtil.isCommandSupported(cmd)) {
      return;
    }

    if (cmd.getCommandPayload() != null && cmd.getCommandPayload().length > 0) {
      log.log(Level.FINE, "Processing command: {0}", cmd.getCommand());

      // Split the command to be executed.
      String[] commandArray = new String(cmd.getCommandPayload(), StandardCharsets.UTF_8)
        .split(" ");
      CommandLine cmdLine = new CommandLine(commandArray[0]);
      if (commandArray.length > 1) {
        cmdLine.addArguments(ArrayUtils.remove(commandArray, 0));
      }
      log.log(Level.FINEST, "Executing command {0}.", cmdLine.toString());

      // Execute the command and capture its output.
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
      ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
      Executor executor = new DefaultExecutor();
      executor.setExitValue(0);
      executor.setStreamHandler(streamHandler);
      executor.setWatchdog(watchdog);
      try {
        executor.execute(cmdLine);
      } catch (IOException e) {
        log.log(Level.SEVERE, "Could not execute command.", e);
      }

      // Send back the reply.
      try {
        MqttCommandReplyPayload reply = new MqttCommandReplyPayload();
        reply.setCommandId(cmd.getId());
        reply.setPayload(outputStream.toByteArray());
        mqttClient.publish(EventType.CONTROL_REPLY, objectMapper.writeValueAsBytes(reply));
      } catch (MqttException | JsonProcessingException e) {
        log.log(Level.SEVERE, "Could not publish command reply.", e);
      }
    } else {
      log.log(Level.WARNING, "Received an {0} command with empty payload.", cmd.getCommand());
    }
  }
}
