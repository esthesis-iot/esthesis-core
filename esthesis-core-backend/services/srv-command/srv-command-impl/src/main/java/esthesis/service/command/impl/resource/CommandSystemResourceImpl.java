package esthesis.service.command.impl.resource;

import esthesis.common.entity.CommandReplyEntity;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.service.CommandService;
import esthesis.service.command.resource.CommandSystemResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class CommandSystemResourceImpl implements CommandSystemResource {

	@Inject
	CommandService commandService;

	@Override
	public ExecuteRequestScheduleInfoDTO save(CommandRequestEntity request) {
		String correlationID = commandService.saveRequest(request).toString();

		return commandService.executeRequest(correlationID);
	}


	@Override
	public List<CommandReplyEntity> getReplies(String correlationId) {
		return commandService.getReplies(correlationId);
	}

	@Override
	public long countCollectedReplies(String correlationId) {
		return commandService.countCollectedReplies(correlationId);
	}
}
