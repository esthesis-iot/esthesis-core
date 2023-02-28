package esthesis.service.command.impl.service;

import esthesis.avro.EsthesisCommandRequestMessage;
import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.entity.CommandReplyEntity;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.resource.TagResource;
import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
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

  @ConfigProperty(name = "quarkus.application.name")
  String appName;

  // The emitter for sending command requests to Kafka. The name of the topic is dynamically set
  // based on application settings.
  @Inject
  @Channel("")
  Emitter<EsthesisCommandRequestMessage> commandRequestEmitter;

  private String KAFKA_CONTROL_REQUEST_TOPIC;

  @PostConstruct
  void init() {
    //TODO we need to setup a Kafka event for when this configuration changes
    KAFKA_CONTROL_REQUEST_TOPIC = settingsResource.findByName(
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
   * Finds devices with the given (partial) hardware Id. The matching algorithm is partial.
   *
   * @param hardwareId The hardware ID to search for.
   */
  public List<DeviceEntity> findDevicesByHardwareId(String hardwareId) {
    return deviceResource.findByHardwareIds(hardwareId, true);
  }

  /**
   * Saves a command to be scheduled for execution. Note that this method does not send the command
   * to the device, see {@link #executeRequest(String)}.
   *
   * @param commandRequestEntity The command request to save.
   */
  public ObjectId saveRequest(CommandRequestEntity commandRequestEntity) {
    return commandRequestService.save(commandRequestEntity).getId();
  }

  public ExecuteRequestScheduleInfoDTO saveRequestAndExecute(
      CommandRequestEntity commandRequestEntity) {
    ObjectId requestId = saveRequest(commandRequestEntity);
    return executeRequest(requestId.toString());
  }

  /**
   * Executes (i.e. sends to the device) a previously saved command.
   *
   * @param requestId The previously saved command request ID.
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

    // Find the devices to which the command should be sent.
    Set<String> hardwareIds = new HashSet<>();

    // Add devices by their hardware IDs.
    if (StringUtils.isNotBlank(request.getHardwareIds())) {
      hardwareIds.addAll(Arrays.asList(request.getHardwareIds().split(",")));
    }

    // Add devices by their tags.
    if (StringUtils.isNotBlank(request.getTags())) {
      hardwareIds.addAll(
          tagResource.findByNames(request.getTags(), false).stream()
              .map(tag -> deviceResource.findByTagId(tag.getId().toString()))
              .flatMap(List::stream).map(DeviceEntity::getHardwareId)
              .toList());
    }

    scheduleInfo.setDevicesMatched(hardwareIds.size());
    log.debug("Found '{}' devices to send command request '{}'.",
        hardwareIds.size(), requestId);

    // Send the command to the devices by queuing an Avro
    // EsthesisControlMessage message in Kafka.
    for (String hardwareId : hardwareIds) {
      EsthesisCommandRequestMessage esthesisCommandRequestMessage =
          avroCommandRequest(request, hardwareId);
      log.debug("Sending command '{}' to device '{}' via Kafka topic '{}'.",
          esthesisCommandRequestMessage, hardwareId,
          KAFKA_CONTROL_REQUEST_TOPIC);
      commandRequestEmitter.send(
          Message.of(esthesisCommandRequestMessage)
              .addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
                  .withTopic(KAFKA_CONTROL_REQUEST_TOPIC)
                  .withKey(hardwareId)
                  .build())
              .addMetadata(TracingMetadata.withCurrent(Context.current())));
      request.setDispatchedOn(Instant.now());
      commandRequestService.save(request);
      scheduleInfo.setDevicesScheduled(scheduleInfo.getDevicesScheduled() + 1);
    }

    return scheduleInfo;
  }

  public Page<CommandRequestEntity> findCommandRequest(Pageable pageable) {
    return commandRequestService.find(pageable);
  }

  public CommandRequestEntity getCommand(String commandId) {
    return commandRequestService.findById(commandId);
  }

  public List<CommandReplyEntity> getReplies(String correlationId) {
    return commandReplyService.findByCorrelationId(correlationId);
  }

  public void deleteCommand(String commandId) {
    commandRequestService.deleteById(commandId);
  }

  public void deleteReply(String replyId) {
    commandReplyService.deleteById(replyId);
  }

  public void purge(Optional<Integer> durationInDays) {
    commandReplyService.purge(durationInDays);
    commandRequestService.purge(durationInDays);
  }

  public long countCollectedReplies(String correlationId) {
    return commandReplyService.countByColumn("corellationId", correlationId);
  }

  public void deleteReplies(String correlationId) {
    commandReplyService.deleteByColumn("correlationId", correlationId);
  }
}
