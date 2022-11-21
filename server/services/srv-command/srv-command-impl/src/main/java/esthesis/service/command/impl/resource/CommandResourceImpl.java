package esthesis.service.command.impl.resource;

import esthesis.common.dto.CommandReply;
import esthesis.service.command.dto.CommandRequest;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.impl.service.CommandService;
import esthesis.service.command.resource.CommandResource;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.awaitility.Awaitility;

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
  public List<CommandReply> getReply(String correlationId) {
    return commandService.getReplies(correlationId);
  }

  @Override
  public String save(CommandRequest request) {
    String correlationID = commandService.saveRequest(request).toString();
    commandService.executeRequest(correlationID);

    return correlationID;
  }

  @Override
  public List<CommandReply> saveAndWait(CommandRequest request, long timeout,
      long pollInterval) {
    // Save the request and schedule its execution.
    String correlationID = commandService.saveRequest(request).toString();
    ExecuteRequestScheduleInfoDTO scheduleInfo = commandService.executeRequest(
        correlationID);

    // Wait for replies to be collected.
    System.out.println(timeout);
    Awaitility.await()
        .atMost(timeout, TimeUnit.MILLISECONDS)
        .pollInterval(pollInterval, TimeUnit.MILLISECONDS)
        .until(() -> {
          System.out.println(
              commandService.countCollectedReplies(correlationID));
          return commandService.countCollectedReplies(correlationID)
              == scheduleInfo.getDevicesScheduled();
        });

    // Collect and return the replies.
    return commandService.getReplies(correlationID);
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
