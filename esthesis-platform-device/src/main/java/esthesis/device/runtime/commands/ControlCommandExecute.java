package esthesis.device.runtime.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.device.runtime.config.AppConstants.Mqtt.EventType;
import esthesis.device.runtime.mqtt.MqttClient;
import esthesis.platform.backend.common.config.AppConstants.CommandReply;
import esthesis.platform.backend.common.device.commands.CommandReplyDTO;
import esthesis.platform.backend.common.device.commands.CommandRequestDTO;
import lombok.extern.java.Log;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;

@Service
@Validated
@Log
public class ControlCommandExecute {
  private static final String COMMAND_NAME = "EXECUTE";
  private final ControlCommandUtil mqttCommandUtil;
  private final MqttClient mqttClient;
  private final ObjectMapper objectMapper;

  public ControlCommandExecute(ControlCommandUtil mqttCommandUtil,
    MqttClient mqttClient, ObjectMapper objectMapper) {
    this.mqttCommandUtil = mqttCommandUtil;
    this.mqttClient = mqttClient;
    this.objectMapper = objectMapper;
  }

  @EventListener
  public void receiveCommand(CommandRequestDTO cmd) {
    if (!cmd.getOperation().equals(COMMAND_NAME) || !mqttCommandUtil.isCommandEnabled(cmd)) {
      return;
    }

    if (StringUtils.isNotBlank(cmd.getArgs())) {
      log.log(Level.FINE, "Processing command: {0}", cmd.getOperation());

      // Split the command to be executed.
      String[] commandArray = cmd.getArgs().split(" ");
      CommandLine cmdLine = new CommandLine(commandArray[0]);
      if (commandArray.length > 1) {
        cmdLine.addArguments(ArrayUtils.remove(commandArray, 0));
      }
      log.log(Level.FINEST, "Executing command {0}.", cmdLine.toString());

      // Execute the command and capture its output.
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
      ExecuteWatchdog watchdog = new ExecuteWatchdog(60L * 1000L);
      Executor executor = new DefaultExecutor();
      executor.setExitValue(0);
      executor.setStreamHandler(streamHandler);
      executor.setWatchdog(watchdog);
      String exceptionMsg = null;
      try {
        executor.execute(cmdLine);
      } catch (IOException e) {
        log.log(Level.SEVERE, "Could not execute command.", e);
        exceptionMsg = e.toString();
      }

      // Send back the reply.
      try {
        CommandReplyDTO reply = new CommandReplyDTO();
        reply.setCommandRequestId(cmd.getId());

        String payload = exceptionMsg != null ? exceptionMsg :
          new String(outputStream.toByteArray());

        reply.setPayload(payload);
        reply.setPayloadType(MediaType.TEXT_PLAIN_VALUE);
        reply.setPayloadEncoding(CommandReply.PAYLOAD_ENCODING_PLAIN);
        mqttClient.publish(EventType.CONTROL_REPLY, objectMapper.writeValueAsBytes(reply));
      } catch (JsonProcessingException e) {
        log.log(Level.SEVERE, "Could not publish command reply.", e);
      }
    } else {
      log.log(Level.WARNING, "Received an {0} command with empty payload.", cmd.getOperation());
    }
  }
}
