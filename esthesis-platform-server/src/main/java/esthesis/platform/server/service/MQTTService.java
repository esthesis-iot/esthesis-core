package esthesis.platform.server.service;

import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONFIGURATION_MQTT;

import esthesis.platform.server.dto.DeviceDTO;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.events.LocalEvent;
import esthesis.platform.server.mapper.MQTTServerMapper;
import esthesis.platform.server.model.MqttServer;
import esthesis.platform.server.repository.MQTTServerRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@Transactional
@Validated
public class MQTTService extends BaseService<MQTTServerDTO, MqttServer> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(MQTTService.class.getName());

  private final MQTTServerMapper mqttServerMapper;
  private final MQTTServerRepository mqttServerRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  public MQTTService(
    MQTTServerMapper mqttServerMapper, MQTTServerRepository mqttServerRepository,
    ApplicationEventPublisher applicationEventPublisher) {
    this.mqttServerMapper = mqttServerMapper;
    this.mqttServerRepository = mqttServerRepository;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public List<MQTTServerDTO> findActive() {
    return mqttServerMapper.map(mqttServerRepository.findAllByState(true));
  }

  @Override
  public MQTTServerDTO save(MQTTServerDTO dto) {
    // Save the MQTT server.
    dto = super.save(dto);

    // Emit an event about this configuration change.
    applicationEventPublisher.publishEvent(new LocalEvent(CONFIGURATION_MQTT));

    return dto;
  }

  @Override
  public MQTTServerDTO deleteById(long id) {
    final MQTTServerDTO mqttServerDTO = super.deleteById(id);

    // Emit an event about this configuration change.
    applicationEventPublisher.publishEvent(new LocalEvent(CONFIGURATION_MQTT));

    return mqttServerDTO;
  }

  public Optional<MQTTServerDTO> matchByTag(DeviceDTO deviceDTO) {
    Optional<MQTTServerDTO> mqttServerDTO;

    if (CollectionUtils.isEmpty(deviceDTO.getTags())) {
      mqttServerDTO = findAll().stream().filter(o -> o.getTags().isEmpty()).findAny();
    } else {
      mqttServerDTO = findAll().stream().filter(o ->
        CollectionUtils.intersection(deviceDTO.getTags(), o.getTags()).size() == deviceDTO.getTags()
          .size()).findAny();
    }

    return mqttServerDTO;
  }
}
