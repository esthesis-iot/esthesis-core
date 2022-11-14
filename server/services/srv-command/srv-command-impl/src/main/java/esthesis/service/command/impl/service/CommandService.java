package esthesis.service.command.impl.service;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.dto.CommandRequest;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.device.dto.Device;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.resource.TagResource;
import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
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

  @Inject
  @Channel("esthesis-control-request")
  Emitter<String> controlRequestEmitter;

  private String KAFKA_CONTROL_REQUEST_TOPIC;

  @PostConstruct
  void init() {
    //TODO we need to setup a Kafka event for when this configuration changes
    KAFKA_CONTROL_REQUEST_TOPIC = settingsResource.findByName(
        NamedSetting.KAFKA_TOPIC_CONTROL_REQUEST).asString();
  }

  /**
   * Counts the number of devices with the given hardware IDs. The matching
   * algorithm is partial.
   *
   * @param hardwareIds A comma-separated list of hardware IDs.
   */
  public Long countDevicesByHardwareIds(String hardwareIds) {
    return deviceResource.countByHardwareIds(hardwareIds, true);
  }

  /**
   * Counts the number of devices with the given tags. The matching algorithm is
   * exact.
   *
   * @param tags A comma-separated list of tag names.
   */
  public Long countDevicesByTags(String tags) {
    return deviceResource.countByTags(tags, false);
  }

  public ObjectId saveRequest(CommandRequest commandRequest) {
    return commandRequestService.save(commandRequest).getId();
  }

  public String executeRequest(String requestId) {
    // Find the command to execute or produce an error if the command can not
    // be found.
    CommandRequest request = commandRequestService.findById(requestId);
    if (request == null) {
      throw new QDoesNotExistException("Command request '{}' does not exist.",
          requestId);
    }

    // Find the devices to which the command should be sent.
    Set<String> hardwareIds = new HashSet<>();

    // Add devices by their hardware IDs.
    if (StringUtils.isNotBlank(request.getHardwareIds())) {
      hardwareIds.addAll(
          deviceResource.findByHardwareIds(request.getHardwareIds(), true)
              .stream().map(Device::getHardwareId).toList());
    }

    // Add devices by their tags.
    if (StringUtils.isNotBlank(request.getTags())) {
      hardwareIds.addAll(
          tagResource.findByNames(request.getTags(), true).stream()
              .map(tag -> deviceResource.findByTagId(tag.getId().toString()))
              .flatMap(List::stream).map(Device::getHardwareId)
              .toList());
    }

    log.debug("Found '{}' devices to send command request '{}'.",
        hardwareIds.size(), requestId);

    // Send the command to the devices.
    for (String hardwareId : hardwareIds) {
      String command = requestId +
          " " + request.getCommandType() + request.getExecutionType()
          + " " + request.getCommand()
          + " " + request.getArguments();
      log.trace("Sending command request '{}' to device '{}' as command '{}' "
              + "via Kafka topic '{}'.", requestId, hardwareId, command,
          KAFKA_CONTROL_REQUEST_TOPIC);

      controlRequestEmitter.send(Message.of(command.trim())
          .addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
              .withTopic(KAFKA_CONTROL_REQUEST_TOPIC)
              .withKey(hardwareId)
              .build())
          .addMetadata(TracingMetadata.withCurrent(Context.current())));

      request.setExecutedOn(Instant.now());
      commandRequestService.save(request);
    }

    return null;
  }
}
