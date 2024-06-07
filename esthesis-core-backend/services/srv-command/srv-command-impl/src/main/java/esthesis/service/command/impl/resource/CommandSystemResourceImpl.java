package esthesis.service.command.impl.resource;

import esthesis.common.entity.CommandReplyEntity;
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
	public String save(CommandRequestEntity request) {
		String correlationID = commandService.saveRequest(request).toString();
		commandService.executeRequest(correlationID);

		return correlationID;
	}

	@Override
	public List<CommandReplyEntity> getReplies(String correlationId) {
		return commandService.getReplies(correlationId);
	}
}
