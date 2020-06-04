package esthesis.platform.server.service;

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
//  private final MqttClientManager mqttClientManager;

  public CommandRequestService(DeviceService deviceService
//    MqttClientManager mqttClientManager
        ) {
    this.deviceService = deviceService;
//    this.mqttClientManager = mqttClientManager;
  }

  public void execute(CommandSpecificationDTO cmd) {
    // Find the devices to dispatch the command to.
    List<DeviceDTO> devices = deviceService.findByHardwareIds(cmd.getHardwareIds());
    devices.addAll(deviceService.findByTags(cmd.getTags()));

    // Persist the command to platform's database and send it to the device
    for (DeviceDTO device : devices) {
      CommandRequestDTO commandRequest = new CommandRequestDTO();
      commandRequest.setCommand(cmd.getCommand());
      commandRequest.setDescription(cmd.getDescription());
      commandRequest.setDevice(device.getId());
      commandRequest = save(commandRequest);
//      mqttClientManager
//        .sendCommand(commandRequest.getId(), device.getHardwareId(), device.getTags(), cmd.getCommand(),
//          cmd.getArguments());
    }
  }
}
