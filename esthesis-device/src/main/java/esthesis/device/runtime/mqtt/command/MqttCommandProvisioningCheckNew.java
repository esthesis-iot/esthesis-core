package esthesis.device.runtime.mqtt.command;

import esthesis.common.config.AppConstants.MqttCommand;
import esthesis.common.device.control.MqttControlCommand;
import esthesis.device.runtime.service.ProvisioningService;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
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
public class MqttCommandProvisioningCheckNew {
  private final ProvisioningService provisioningService;
  private final MqttCommandUtil mqttCommandUtil;

  public MqttCommandProvisioningCheckNew(
    ProvisioningService provisioningService,
    MqttCommandUtil mqttCommandUtil) {
    this.provisioningService = provisioningService;
    this.mqttCommandUtil = mqttCommandUtil;
  }

  @EventListener
  public void receiveCommand(MqttControlCommand cmd)
  throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException,
         SignatureException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
    if (cmd.getCommand() != MqttCommand.PROVISIONING_CHECK_NEW || !mqttCommandUtil.isCommandSupported(cmd)) {
      return;
    }

    log.log(Level.FINE, "Processing command: {0}", cmd.getCommand());
    provisioningService.provisioning();

  }
}
