package esthesis.service.command.impl.resource;

import esthesis.service.command.dto.CommandReply;
import esthesis.service.command.dto.CommandRequest;
import esthesis.service.command.impl.service.CommandReplyService;
import esthesis.service.command.impl.service.CommandRequestService;
import esthesis.service.command.impl.service.CommandService;
import esthesis.service.command.resource.CommandResource;
import javax.inject.Inject;

public class CommandResourceImpl implements CommandResource {

  @Inject
  CommandRequestService commandRequestService;

  @Inject
  CommandReplyService commandReplyService;

  @Inject
  CommandService commandService;

  @Override
  public CommandReply create(CommandRequest request) {
    return null;
  }

  @Override
  public Long countDevicesByHardwareIds(String hardwareIds) {
    return commandService.countDevicesByHardwareIds(hardwareIds);
  }

  @Override
  public Long countDevicesByTags(String hardwareIds) {
    return commandService.countDevicesByTags(hardwareIds);
  }
}
