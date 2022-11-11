package esthesis.service.command.impl.resource;

import esthesis.common.dto.CommandReply;
import esthesis.common.dto.CommandRequest;
import esthesis.service.command.impl.service.CommandReplyService;
import esthesis.service.command.impl.service.CommandRequestService;
import esthesis.service.command.impl.service.CommandService;
import esthesis.service.command.resource.CommandResource;
import esthesis.service.dataflow.resource.DataflowResource;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

public class CommandResourceImpl implements CommandResource {

  @Inject
  CommandRequestService commandRequestService;

  @Inject
  CommandReplyService commandReplyService;

  @Inject
  CommandService commandService;

  @Inject
  @RestClient
  DataflowResource dataflowResource;

  @Override
  public String dispatch(CommandRequest request) {
    String correlationID = commandRequestService.save(request).getId()
        .toString();

    return correlationID;
  }

  @Override
  public CommandReply dispatchAndWait(CommandRequest request) {
    String correlationID = commandRequestService.save(request).getId()
        .toString();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    return new CommandReply();
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
