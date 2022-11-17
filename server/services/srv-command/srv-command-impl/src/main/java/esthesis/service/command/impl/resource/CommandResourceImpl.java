package esthesis.service.command.impl.resource;

import esthesis.service.command.dto.CommandReply;
import esthesis.service.command.dto.CommandRequest;
import esthesis.service.command.impl.service.CommandService;
import esthesis.service.command.resource.CommandResource;
import java.util.List;
import javax.inject.Inject;
import org.bson.types.ObjectId;

public class CommandResourceImpl implements CommandResource {

  @Inject
  CommandService commandService;

  @Override
  public String save(CommandRequest request) {
    ObjectId correlationID = commandService.saveRequest(request);
    commandService.executeRequest(correlationID.toString());

    return correlationID.toString();
  }

  @Override
  public List<CommandReply> saveAndWait(CommandRequest request) {
    ObjectId correlationID = commandService.saveRequest(request);

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
