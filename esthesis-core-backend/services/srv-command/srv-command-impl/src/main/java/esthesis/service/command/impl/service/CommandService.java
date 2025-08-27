package esthesis.service.command.impl.service;

import static esthesis.core.common.AppConstants.ROLE_SYSTEM;
import static esthesis.core.common.AppConstants.Security.Category.COMMAND;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;

import esthesis.common.avro.EsthesisCommandRequestMessage;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandReplyEntity;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.resource.TagResource;
import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Service for managing commands.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class CommandService {

	@Inject
	@RestClient
	DeviceResource deviceResource;

	@Inject
	@RestClient
	SettingsResource settingsResource;

	@Inject
	@RestClient
	TagResource tagResource;

	@Inject
	CommandRequestService commandRequestService;

	@Inject
	CommandReplyService commandReplyService;

	@Inject
	@Channel("esthesis-command-request")
	Emitter<EsthesisCommandRequestMessage> commandRequestEmitter;

	private String kafkaTopicCommandRequest;

	@PostConstruct
	void init() {
		//TODO we need to setup a Kafka event for when this configuration changes
		kafkaTopicCommandRequest = settingsResource.findByName(
			NamedSetting.KAFKA_TOPIC_COMMAND_REQUEST).asString();
	}

	/**
	 * Converts a {@link CommandRequestEntity} into a {@link EsthesisCommandRequestMessage} AVRO
	 * message.
	 *
	 * @param request    The command request to convert.
	 * @param hardwareId The hardware ID of the device to send the command to.
	 */
	private EsthesisCommandRequestMessage avroCommandRequest(
		CommandRequestEntity request, String hardwareId) {
		return EsthesisCommandRequestMessage.newBuilder()
			.setId(request.getId().toString())
			.setHardwareId(hardwareId)
			.setCommandType(request.getCommandType())
			.setExecutionType(request.getExecutionType())
			.setCommand(request.getCommand())
			.setArguments(request.getArguments())
			.setCreatedAt(Instant.now().toString())
			.build();
	}

	/**
	 * Finds devices with the given hardware ID. The matching algorithm is partial.
	 *
	 * @param hardwareId The hardware ID to search for.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = READ)
	public List<DeviceEntity> findDevicesByHardwareId(String hardwareId) {
		return deviceResource.findByHardwareIds(hardwareId);
	}

	/**
	 * Saves a command to be scheduled for execution. Note that this method does not send the command
	 * to the device, see {@link #executeRequest(String)}.
	 *
	 * @param cre The command request to save.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = CREATE)
	public ObjectId saveRequest(CommandRequestEntity cre) {
		// Set a default description for the command according to the command type, if none is provided.
		if (StringUtils.isBlank(cre.getDescription())) {
			switch (cre.getCommandType()) {
				case e:
					cre.setDescription("Execute");
					break;
				case f:
					cre.setDescription("Firmware update");
					break;
				case r:
					cre.setDescription("Reboot");
					break;
				case s:
					cre.setDescription("Shutdown");
					break;
				case p:
					cre.setDescription("Ping");
					break;
				case h:
					cre.setDescription("Health report");
					break;
				default:
					cre.setDescription("Unknown");
					break;
			}
		}
		return commandRequestService.save(cre).getId();
	}

	/**
	 * Saves a command and executes it immediately.
	 *
	 * @param commandRequestEntity The command request to save and execute.
	 * @return Information regarding the scheduling of the execution.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = CREATE)
	public ExecuteRequestScheduleInfoDTO saveRequestAndExecute(
		CommandRequestEntity commandRequestEntity) {
		log.debug("Saving command request '{}'.", commandRequestEntity);
		ObjectId requestId = saveRequest(commandRequestEntity);
		log.debug("Saved command request '{}' with ID '{}'.", commandRequestEntity, requestId);
		return executeRequest(requestId.toString());
	}

	/**
	 * Executes (i.e. sends to the device) a previously saved command.
	 *
	 * @param requestId The previously saved command request ID.
	 * @return Information regarding the scheduling of the execution.
	 */
	public ExecuteRequestScheduleInfoDTO executeRequest(String requestId) {
		// Find the command to execute or produce an error if the command can not be found.
		CommandRequestEntity request = commandRequestService.findById(requestId);
		if (request == null) {
			throw new QDoesNotExistException("Command request '{}' does not exist.",
				requestId);
		}

		// Provide information regarding the scheduling of the execution.
		ExecuteRequestScheduleInfoDTO scheduleInfo = new ExecuteRequestScheduleInfoDTO();
		scheduleInfo.setCorrelationId(requestId);

		// Find the devices to which the command should be sent.
		Set<String> hardwareIds = new HashSet<>();

		// Add devices by their hardware IDs.
		if (StringUtils.isNotBlank(request.getHardwareIds())) {
			hardwareIds.addAll(Arrays.asList(request.getHardwareIds().split(",")));
		}

		// Add devices by their tags.
		if (StringUtils.isNotBlank(request.getTags())) {
			hardwareIds.addAll(
				tagResource.findByIds(request.getTags()).stream()
					.map(tag -> deviceResource.findByTagId(tag.getId().toString()))
					.flatMap(List::stream).map(DeviceEntity::getHardwareId)
					.toList());
		}

		scheduleInfo.setDevicesMatched(hardwareIds.size());
		log.trace("Found '{}' devices to send command request '{}'.",
			hardwareIds.size(), requestId);

		// Send the command to the devices by queuing an Avro EsthesisControlMessage message in Kafka.
		for (String hardwareId : hardwareIds) {
			EsthesisCommandRequestMessage esthesisCommandRequestMessage =
				avroCommandRequest(request, hardwareId);
			log.trace("Sending command '{}' to device '{}' via Kafka topic '{}'.",
				esthesisCommandRequestMessage, hardwareId, kafkaTopicCommandRequest);
			commandRequestEmitter.send(
				Message.of(esthesisCommandRequestMessage)
					.addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
						.withTopic(kafkaTopicCommandRequest)
						.withKey(hardwareId)
						.build())
					.addMetadata(TracingMetadata.withCurrent(Context.current())));
			scheduleInfo.setDevicesScheduled(scheduleInfo.getDevicesScheduled() + 1);
		}
		request.setDispatchedOn(Instant.now());
		commandRequestService.save(request);

		return scheduleInfo;
	}

	/**
	 * Finds command requests.
	 *
	 * @param pageable Paging parameters.
	 * @return A page of command requests.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = READ)
	public Page<CommandRequestEntity> findCommandRequest(Pageable pageable) {
		return commandRequestService.find(pageable);
	}

	/**
	 * Finds a command request.
	 *
	 * @param commandId The ID of the command to find.
	 * @return The command request.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = READ)
	public CommandRequestEntity getCommand(String commandId) {
		return commandRequestService.findById(commandId);
	}

	/**
	 * Finds command replies.
	 *
	 * @param correlationId The correlation ID of the command replies to find.
	 * @return A list of command replies.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = READ)
	public List<CommandReplyEntity> getReplies(String correlationId) {
		return commandReplyService.findByCorrelationId(correlationId);
	}

	/**
	 * Deletes a command.
	 *
	 * @param commandId The ID of the command to delete.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = DELETE)
	public void deleteCommand(String commandId) {
		commandRequestService.deleteById(commandId);
	}

	/**
	 * Deletes a command reply.
	 *
	 * @param replyId The ID of the command reply to delete.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = DELETE)
	public void deleteReply(String replyId) {
		commandReplyService.deleteById(replyId);
	}

	/**
	 * Purges command requests and replies older than a certain duration.
	 *
	 * @param durationInDays The duration in days.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = DELETE)
	public void purge(int durationInDays) {
		commandReplyService.purge(durationInDays);
		commandRequestService.purge(durationInDays);
	}

	/**
	 * Counts the number of collected replies.
	 *
	 * @param correlationId The correlation ID of the replies.
	 * @return The number of replies.
	 */
	public long countCollectedReplies(String correlationId) {
		return commandReplyService.countByColumn("correlationId", correlationId);
	}

	/**
	 * Deletes all replies for a given correlation ID.
	 *
	 * @param correlationId The correlation ID of the replies to delete.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = DELETE)
	public void deleteReplies(String correlationId) {
		commandReplyService.deleteByColumn("correlationId", correlationId);
	}

	/**
	 * Replays a command by creating a new command matching the old command and executing it
	 * immediately.
	 *
	 * @param sourceCommandId The ID of the source command to replay.
	 */
	@ErnPermission(bypassForRoles = {ROLE_SYSTEM}, category = COMMAND, operation = CREATE)
	public void replayCommand(String sourceCommandId) {
		CommandRequestEntity sourceCommand = commandRequestService.findById(sourceCommandId);
		if (sourceCommand == null) {
			throw new QDoesNotExistException("Command request '{}' does not exist.", sourceCommandId);
		}

		// Create a new command request based on the source command.
		CommandRequestEntity replayCommand = new CommandRequestEntity();
		replayCommand.setCommandType(sourceCommand.getCommandType());
		replayCommand.setExecutionType(sourceCommand.getExecutionType());
		replayCommand.setCommand(sourceCommand.getCommand());
		replayCommand.setArguments(sourceCommand.getArguments());
		replayCommand.setHardwareIds(sourceCommand.getHardwareIds());
		replayCommand.setTags(sourceCommand.getTags());
		replayCommand.setCreatedOn(Instant.now());

		// Save the new command request.
		saveRequestAndExecute(replayCommand);
	}
}
