package esthesis.device.runtime.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.device.runtime.config.AppConstants.Mqtt.EventType;
import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.mqtt.MqttClient;
import esthesis.platform.backend.common.config.AppConstants.Device;
import esthesis.platform.backend.common.config.AppConstants.Device.CommandType;
import esthesis.platform.backend.common.device.commands.CommandReplyDTO;
import esthesis.platform.backend.common.device.commands.CommandRequestDTO;
import java.io.IOException;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Log
public class ControlCommandReboot {

  private static final String COMMAND_NAME = CommandType.REBOOT;
  private final AppProperties appProperties;
  private final ControlCommandUtil mqttCommandUtil;
  private final MqttClient mqttClient;
  private final ObjectMapper objectMapper;

  public ControlCommandReboot(AppProperties appProperties,
    ControlCommandUtil mqttCommandUtil, MqttClient mqttClient,
    ObjectMapper objectMapper) {
    this.appProperties = appProperties;
    this.mqttCommandUtil = mqttCommandUtil;
    this.mqttClient = mqttClient;
    this.objectMapper = objectMapper;
  }

  @EventListener
  public void receiveCommand(CommandRequestDTO cmd) throws IOException {
    if (!cmd.getOperation().equals(COMMAND_NAME) || !mqttCommandUtil.isCommandEnabled(cmd)) {
      return;
    }
    log.log(Level.FINE, "Processing command: {0}", cmd.getOperation());

    try {
      CommandReplyDTO reply = new CommandReplyDTO();
      reply.setCommandRequestId(cmd.getId());
      reply.setPayload(
        (COMMAND_NAME + " received. Will execute: " + appProperties.getRebootCommand()));
      reply.setPayloadType(MediaType.TEXT_PLAIN_VALUE);
      reply.setPayloadEncoding(Device.CommandReply.PAYLOAD_ENCODING_PLAIN);
      mqttClient.publish(EventType.CONTROL_REPLY, objectMapper.writeValueAsBytes(reply));
    } catch (JsonProcessingException e) {
      log.log(Level.SEVERE, "Could not publish command reply.", e);
    }

    Runtime.getRuntime().exec(appProperties.getRebootCommand().split(" "));
  }
}
