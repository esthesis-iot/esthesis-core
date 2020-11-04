package esthesis.platform.server.service;

import esthesis.common.device.commands.CommandRequestDTO;
import esthesis.common.device.dto.DeviceDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Transactional
public class ControlService {
  private final DeviceService deviceService;

  public ControlService(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @Async
  public void execute(CommandRequestDTO cmd, List<String> hardwareIds, List<String> tags) {
    // Find the devices to dispatch the command to.
    List<DeviceDTO> devices = deviceService.findByHardwareIds(hardwareIds);
    devices.addAll(deviceService.findByTags(tags));

    // Persist the command to platform's database and send it to the device
    for (DeviceDTO device : devices) {
      
    }
  }
}
