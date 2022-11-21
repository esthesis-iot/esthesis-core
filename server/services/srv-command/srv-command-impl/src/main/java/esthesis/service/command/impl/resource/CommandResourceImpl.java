package esthesis.service.command.impl.resource;

import esthesis.common.dto.CommandReply;
import esthesis.service.command.dto.CommandRequest;
import esthesis.service.command.impl.service.CommandService;
import esthesis.service.command.resource.CommandResource;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.bson.types.ObjectId;

public class CommandResourceImpl implements CommandResource {

  @Inject
  CommandService commandService;

  @GET
  @Override
  @Path("/v1/command/find")
  public Page<CommandRequest> find(@BeanParam Pageable pageable) {
    return commandService.findCommandRequest(pageable);
  }

  @Override
  public CommandRequest getCommand(String commandId) {
    return commandService.getCommand(commandId);
  }

  @Override
  public CommandReply getReply(String correlationId) {
    return commandService.getReply(correlationId);
  }

  @Override
  public String save(CommandRequest request) {
    ObjectId correlationID = commandService.saveRequest(request);
    commandService.executeRequest(correlationID.toString());

    return correlationID.toString();
  }

  @Override
  public List<CommandReply> saveAndWait(CommandRequest request,
      Optional<Long> timeout) {
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

  @Override
  public void deleteCommand(String commandId) {
    commandService.deleteCommand(commandId);
  }

  @Override
  public void deleteReply(String replyId) {
    commandService.deleteReply(replyId);
  }

  @Override
  public void purge(Optional<Integer> durationInDays) {
    commandService.purge(durationInDays);
  }

  @Override
  public void purgeAll() {
    commandService.purge(Optional.empty());
  }
}
