package esthesis.service.command.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.command.entity.CommandReplyEntity;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.service.CommandService;
import esthesis.service.command.resource.CommandResource;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.entity.DeviceEntity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;

@Slf4j
public class CommandResourceImpl implements CommandResource {

	@Inject
	CommandService commandService;

	@GET
	@Override
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.COMMAND, op = Operation.READ, msg = "Search commands",
		log = AuditLogType.DATA_IN)
	public Page<CommandRequestEntity> find(@BeanParam Pageable pageable) {
		return commandService.findCommandRequest(pageable, true);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.COMMAND, op = Operation.READ, msg = "Get command")
	public CommandRequestEntity getCommand(String commandId) {
		return commandService.getCommand(commandId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.COMMAND, op = Operation.READ, msg = "Get reply")
	public List<CommandReplyEntity> getReply(String correlationId) {
		return commandService.getReplies(correlationId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.COMMAND, op = Operation.WRITE, msg = "Save command")
	public String save(CommandRequestEntity request) {
		String correlationID = commandService.saveRequest(request).toString();
		commandService.executeRequest(correlationID);

		return correlationID;
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.COMMAND, op = Operation.WRITE, msg = "Save command")
	public List<CommandReplyEntity> saveAndWait(CommandRequestEntity request, long timeout,
		long pollInterval) {
		log.debug("saveAndWait: request={}, timeout={}, pollInterval={}", request, timeout,
			pollInterval);
		// Save the request and schedule its execution.
		String correlationID = commandService.saveRequest(request).toString();
		ExecuteRequestScheduleInfoDTO scheduleInfo = commandService.executeRequest(correlationID);
		log.debug("saveAndWait: scheduleInfo={}", scheduleInfo);

		// Wait for replies to be collected.
		Instant startTime = Instant.now();
		try {
			Awaitility.await()
				.atMost(timeout, TimeUnit.MILLISECONDS)
				.pollInterval(pollInterval, TimeUnit.MILLISECONDS)
				.until(() -> commandService.countCollectedReplies(correlationID)
					== scheduleInfo.getDevicesScheduled());
		} catch (org.awaitility.core.ConditionTimeoutException e) {
			// Ignore Awaitility timeout exception.
		}
		log.debug("Exiting, total wait time was {} ms,", Instant.now().toEpochMilli()
			- startTime.toEpochMilli());

		// Collect and return the replies.
		return commandService.getReplies(correlationID);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<DeviceEntity> findDevicesByHardwareId(String hardwareId) {
		return commandService.findDevicesByHardwareId(hardwareId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.COMMAND, op = Operation.DELETE, msg = "Delete command")
	public void deleteCommand(String commandId) {
		commandService.deleteCommand(commandId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.COMMAND, op = Operation.DELETE, msg = "Delete command reply")
	public void deleteReply(String replyId) {
		commandService.deleteReply(replyId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.COMMAND, op = Operation.DELETE, msg = "Delete command replies")
	public void deleteReplies(String correlationId) {
		commandService.deleteReplies(correlationId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.COMMAND, op = Operation.DELETE, msg = "Purge commands")
	public void purge(int durationInDays) {
		commandService.purge(durationInDays);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.COMMAND, op = Operation.DELETE, msg = "Purge commands")
	public void purge() {
		commandService.purge(0);
	}

	@Override
	public void replayCommand(String sourceCommandId) {
		commandService.replayCommand(sourceCommandId);
	}

}
