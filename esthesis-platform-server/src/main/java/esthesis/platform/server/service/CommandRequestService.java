package esthesis.platform.server.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
//public class CommandRequestService extends BaseService<CommandRequestDTO, CommandRequest> {
public class CommandRequestService {

  private final DeviceService deviceService;
  private final DTService dtService;

  public CommandRequestService(DeviceService deviceService,
    DTService dtService) {
    this.deviceService = deviceService;
    this.dtService = dtService;
  }

//  @Async
//  public void execute(CommandRequestDTO cmd, List<String> hardwareIds, List<String> tags) {
//    // Find the devices to dispatch the command to.
//    List<DeviceDTO> devices = deviceService.findByHardwareIds(hardwareIds);
//    devices.addAll(deviceService.findByTags(tags));
//
//    // Persist the command to platform's database and send it to the device
//    for (DeviceDTO device : devices) {
//      dtService.
//    }
//  }
}
