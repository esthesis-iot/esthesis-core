package esthesis.service.dt.impl.resource;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.dt.dto.DTValueReplyDTO;
import esthesis.service.dt.impl.service.DTService;
import esthesis.service.dt.resource.DTResource;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Implementation of the {@link DTResource} interface.
 */
public class DTResourceImpl implements DTResource {

	public static final String HEADER_CORRELATION_ID = "correlation-id";

	@Inject
	DTService dtService;

	@Inject
	@RestClient
	TagResource tagResource;

	/**
	 * Create a command request entity with the given command type and execution type.
	 *
	 * @param commandType   The type of command to create this request for.
	 * @param executionType The type of execution to create this request for.
	 * @return The created command request entity.
	 */
	private CommandRequestEntity createCommandRequest(CommandType commandType,
		ExecutionType executionType) {
		return createCommandRequest(commandType, executionType, "");
	}

	/**
	 * Create a command request entity with the given command type, execution type and command.
	 *
	 * @param commandType   The type of command to create this request for.
	 * @param executionType The type of execution to create this request for.
	 * @param command       The command to create this request for.
	 * @return The created command request entity.
	 */
	private CommandRequestEntity createCommandRequest(CommandType commandType,
		ExecutionType executionType, String command) {
		return new CommandRequestEntity()
			.setCreatedOn(Instant.now())
			.setCommandType(commandType)
			.setExecutionType(executionType)
			.setCommand(command)
			.setArguments("");
	}

	/**
	 * Get the command response for the given command request. If a command is asynchronous, a
	 * response is immediately returned with the correlation ID in the headers. Otherwise, the
	 * response is returned with the command replies in the body (and the correlation ID in the
	 * headers).
	 *
	 * @param commandRequest The command request to get the response for.
	 * @return The response for the given command request.
	 */
	private Response getCommandResponse(CommandRequestEntity commandRequest) {
		ExecuteRequestScheduleInfoDTO scheduleInfo = dtService.saveCommandRequest(commandRequest);

		// If async execution replies an empty body payload with correlation ID on the HEADER.
		//  Otherwise, if sync, the body is the command replies
		if (ExecutionType.a.equals(commandRequest.getExecutionType())) {
			return Response.ok().header(HEADER_CORRELATION_ID, scheduleInfo.getCorrelationId()).build();
		} else {
			return Response.ok(dtService.waitAndGetReplies(scheduleInfo))
				.header(HEADER_CORRELATION_ID, scheduleInfo.getCorrelationId())
				.build();
		}
	}

	/**
	 * Convert a tag name to tag ID.
	 *
	 * @param tagName The name of the tag to convert.
	 * @return The ID of the tag, or throws an exception if the tag name does not exist.
	 */
	private String tagNameToTagId(String tagName) {
		TagEntity tagEntity = tagResource.findByName(tagName);
		if (tagEntity == null) {
			throw new QDoesNotExistException("Tag with name '{}' does not exist.", tagName);
		} else {
			return tagEntity.getId().toHexString();
		}
	}

	@Override
	public Response findJSON(String hardwareId, String category, String measurement) {
		DTValueReplyDTO dtValueReplyDTO = dtService.find(hardwareId, category, measurement);
		if (dtValueReplyDTO != null) {
			return Response.ok(dtValueReplyDTO).build();
		} else {
			return Response.status(Status.NO_CONTENT).build();
		}
	}

	@Override
	public Response findPlain(String hardwareId, String category, String measurement) {
		DTValueReplyDTO dtValueReplyDTO = dtService.find(hardwareId, category, measurement);
		if (dtValueReplyDTO != null) {
			return Response.ok(dtValueReplyDTO.getValue().toString()).build();
		} else {
			return Response.status(Status.NO_CONTENT).build();
		}
	}

	@Override
	public Response findAllJSON(String hardwareId, String category) {
		List<DTValueReplyDTO> values = dtService.findAll(hardwareId, category);
		if (values != null && !CollectionUtils.isEmpty(values)) {
			return Response.ok(values).build();
		} else {
			return Response.status(Status.NO_CONTENT).build();
		}
	}

	@Override
	public Response findAllPlain(String hardwareId, String category) {
		String values = dtService.findAll(hardwareId, category).stream().map(val ->
			val.getMeasurement() + "=" +
				val.getValue()).collect(Collectors.joining("\n"));
		if (StringUtils.isNotBlank(values)) {
			return Response.ok(values).build();
		} else {
			return Response.status(Status.NO_CONTENT).build();
		}
	}

	@Override
	public Response findMeasurements(String hardwareId, String category) {
		String values = dtService.findAll(hardwareId, category).stream().map(
			DTValueReplyDTO::getMeasurement).collect(Collectors.joining("\n"));
		if (StringUtils.isNotBlank(values)) {
			return Response.ok(values).build();
		} else {
			return Response.status(Status.NO_CONTENT).build();
		}
	}

	@Override
	public Response executeCommandByHardwareId(String hardwareId, String command, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.e,
			(async ? ExecutionType.a : ExecutionType.s), command);
		commandRequest.setHardwareIds(hardwareId);
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response executeCommandByTag(String tag, String command, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.e,
			(async ? ExecutionType.a : ExecutionType.s), command);
		commandRequest.setTags(tagNameToTagId(tag));
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response pingCommandByHardwareId(String hardwareId, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.p,
			(async ? ExecutionType.a : ExecutionType.s));
		commandRequest.setHardwareIds(hardwareId);
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response pingCommandByTag(String tag, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.p,
			(async ? ExecutionType.a : ExecutionType.s));
		commandRequest.setTags(tagNameToTagId(tag));
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response shutdownCommandByHardwareId(String hardwareId, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.s,
			(async ? ExecutionType.a : ExecutionType.s));
		commandRequest.setHardwareIds(hardwareId);
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response shutdownCommandByTag(String tag, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.s,
			(async ? ExecutionType.a : ExecutionType.s));
		commandRequest.setTags(tagNameToTagId(tag));
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response rebootCommandByHardwareId(String hardwareId, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.r,
			(async ? ExecutionType.a : ExecutionType.s));
		commandRequest.setHardwareIds(hardwareId);
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response rebootCommandByTag(String tag, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.r,
			(async ? ExecutionType.a : ExecutionType.s));
		commandRequest.setTags(tagNameToTagId(tag));
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response firmwareCommandByHardwareId(String hardwareId, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.f,
			(async ? ExecutionType.a : ExecutionType.s));
		commandRequest.setHardwareIds(hardwareId);
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response firmwareCommandByTag(String tag, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.f,
			(async ? ExecutionType.a : ExecutionType.s));
		commandRequest.setTags(tagNameToTagId(tag));
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response healthCommandByHardwareId(String hardwareId, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.h,
			(async ? ExecutionType.a : ExecutionType.s));
		commandRequest.setHardwareIds(hardwareId);
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response healthCommandByTag(String tag, boolean async) {
		CommandRequestEntity commandRequest = createCommandRequest(CommandType.h,
			(async ? ExecutionType.a : ExecutionType.s));
		commandRequest.setTags(tagNameToTagId(tag));
		return getCommandResponse(commandRequest);
	}

	@Override
	public Response getCommandReply(String correlationId) {
		return Response.ok(dtService.getReplies(correlationId)).build();
	}
}
