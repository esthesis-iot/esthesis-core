package esthesis.service.command.impl.resource;

import esthesis.common.entity.CommandReplyEntity;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.service.CommandService;
import esthesis.service.command.resource.CommandResource;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.entity.DeviceEntity;
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
  public Page<CommandRequestEntity> find(@BeanParam Pageable pageable) {
    return commandService.findCommandRequest(pageable);
  }

  @Override
  public CommandRequestEntity getCommand(String commandId) {
    return commandService.getCommand(commandId);
  }

  @Override
  public List<CommandReplyEntity> getReply(String correlationId) {
    return commandService.getReplies(correlationId);
  }

  @Override
  public String save(CommandRequestEntity request) {
    String correlationID = commandService.saveRequest(request).toString();
    commandService.executeRequest(correlationID);

    return correlationID;
  }

  @Override
  public List<CommandReplyEntity> saveAndWait(CommandRequestEntity request, long timeout,
      long pollInterval) {
    // Save the request and schedule its execution.
    String correlationID = commandService.saveRequest(request).toString();
    ExecuteRequestScheduleInfoDTO scheduleInfo = commandService.executeRequest(
        correlationID);

    // Wait for replies to be collected.
    Awaitility.await()
        .atMost(timeout, TimeUnit.MILLISECONDS)
        .pollInterval(pollInterval, TimeUnit.MILLISECONDS)
        .until(() -> {
          return commandService.countCollectedReplies(correlationID)
              == scheduleInfo.getDevicesScheduled();
        });

    // Collect and return the replies.
    return commandService.getReplies(correlationID);
  }

  @Override
  public List<DeviceEntity> findDevicesByHardwareId(String hardwareId) {
    return commandService.findDevicesByHardwareId(hardwareId);
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
  public void deleteReplies(String correlationId) {
    commandService.deleteReplies(correlationId);
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
