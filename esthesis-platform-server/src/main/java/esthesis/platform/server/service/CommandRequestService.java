package esthesis.platform.server.service;

import esthesis.platform.server.cluster.mqtt.MqttClientManager;
import esthesis.platform.server.dto.CommandRequestDTO;
import esthesis.platform.server.dto.CommandSpecificationDTO;
import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.model.CommandRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Transactional
public class CommandRequestService extends BaseService<CommandRequestDTO, CommandRequest> {

  private final DeviceService deviceService;
  private final MqttClientManager mqttClientManager;

  public CommandRequestService(DeviceService deviceService, MqttClientManager mqttClientManager) {
    this.deviceService = deviceService;
    this.mqttClientManager = mqttClientManager;
  }

  public void execute(CommandSpecificationDTO cmd) {
    // Find the devices to dispatch the command to.
    List<DeviceDTO> devices = deviceService.findByHardwareIds(cmd.getHardwareIds());
    devices.addAll(deviceService.findByTags(cmd.getTags()));

    // Persist the command to platform's database.
    CommandRequestDTO commandRequest = new CommandRequestDTO();
    for (DeviceDTO device : devices) {
      commandRequest.setCommand(cmd.getCommand());
      commandRequest.setDescription(cmd.getDescription());
      commandRequest.setDevice(device.getId());
      commandRequest = save(commandRequest);
    }

    // Send the command to devices via MQTT.
    for (DeviceDTO deviceDTO : devices) {
      mqttClientManager
        .sendCommand(commandRequest.getId(), deviceDTO.getHardwareId(), deviceDTO.getTags(), cmd.getCommand(),
          cmd.getArguments());
    }
  }
}
