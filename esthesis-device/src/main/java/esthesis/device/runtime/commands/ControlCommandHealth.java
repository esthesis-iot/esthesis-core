package esthesis.device.runtime.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.config.AppConstants.CommandReply;
import esthesis.common.device.commands.CommandReplyDTO;
import esthesis.common.device.commands.CommandRequestDTO;
import esthesis.device.runtime.config.AppConstants.Mqtt.EventType;
import esthesis.device.runtime.health.HealthMetadataCollector;
import esthesis.device.runtime.mqtt.MqttClient;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.logging.Level;

@Service
@Validated
@Log
public class ControlCommandHealth {

  private final static String COMMAND_NAME = "HEALTH";
  private final HealthMetadataCollector healthMetadataCollector;
  private final ControlCommandUtil mqttCommandUtil;
  private final MqttClient mqttClient;
  private final ObjectMapper objectMapper;

  public ControlCommandHealth(
    HealthMetadataCollector healthMetadataCollector,
    ControlCommandUtil mqttCommandUtil, MqttClient mqttClient,
    ObjectMapper objectMapper) {
    this.healthMetadataCollector = healthMetadataCollector;
    this.mqttCommandUtil = mqttCommandUtil;
    this.mqttClient = mqttClient;
    this.objectMapper = objectMapper;
  }

  @EventListener
  public void receiveCommand(CommandRequestDTO cmd) {
    if (!cmd.getOperation().equals(COMMAND_NAME) || !mqttCommandUtil.isCommandEnabled(cmd)) {
      return;
    }

    log.log(Level.FINE, "Processing command: {0}", cmd.getOperation());

    // Send back the reply.
    try {
      CommandReplyDTO reply = new CommandReplyDTO();
      reply.setCommandRequestId(cmd.getId());
      reply.setPayloadType(MediaType.APPLICATION_JSON_VALUE);
      reply.setPayloadEncoding(CommandReply.PAYLOAD_ENCODING_PLAIN);
      reply.setPayload(new String(healthMetadataCollector.collectHealthData()));
      mqttClient.publish(EventType.CONTROL_REPLY, objectMapper.writeValueAsBytes(reply));
    } catch (JsonProcessingException e) {
      log.log(Level.SEVERE, "Could not publish command reply.", e);
    }
  }
}
