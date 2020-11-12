package esthesis.device.runtime.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.config.AppConstants.CommandReply;
import esthesis.common.device.commands.CommandReplyDTO;
import esthesis.common.device.commands.CommandRequestDTO;
import esthesis.device.runtime.config.AppConstants.Mqtt.EventType;
import esthesis.device.runtime.mqtt.MqttClient;
import esthesis.device.runtime.service.ProvisioningService;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;

@Service
@Validated
@Log
public class ControlCommandProvisioningCheckNew {

  private static final String COMMAND_NAME = "PROVISIONING_CHECK_NEW";
  private final ProvisioningService provisioningService;
  private final ControlCommandUtil mqttCommandUtil;
  private final MqttClient mqttClient;
  private final ObjectMapper objectMapper;

  public ControlCommandProvisioningCheckNew(
    ProvisioningService provisioningService,
    ControlCommandUtil mqttCommandUtil, MqttClient mqttClient,
    ObjectMapper objectMapper) {
    this.provisioningService = provisioningService;
    this.mqttCommandUtil = mqttCommandUtil;
    this.mqttClient = mqttClient;
    this.objectMapper = objectMapper;
  }

  @EventListener
  public void receiveCommand(CommandRequestDTO cmd)
  throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException,
         SignatureException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
    if (!cmd.getOperation().equals(COMMAND_NAME) || !mqttCommandUtil.isCommandEnabled(cmd)) {
      return;
    }

    log.log(Level.FINE, "Processing command: {0}", cmd.getOperation());
    final Long provisioningPackageFound = provisioningService.provisioning();

    // Send back the reply.
    try {
      CommandReplyDTO reply = new CommandReplyDTO();
      reply.setCommandRequestId(cmd.getId());
      if (provisioningPackageFound != null) {
        reply.setPayload("No provisioning package found");
      } else {
        reply.setPayload(String.valueOf(provisioningPackageFound));
      }
      reply.setPayloadType(MediaType.TEXT_PLAIN_VALUE);
      reply.setPayloadEncoding(CommandReply.PAYLOAD_ENCODING_PLAIN);
      mqttClient.publish(EventType.CONTROL_REPLY, objectMapper.writeValueAsBytes(reply));
    } catch (JsonProcessingException e) {
      log.log(Level.SEVERE, "Could not publish command reply.", e);
    }
  }
}
