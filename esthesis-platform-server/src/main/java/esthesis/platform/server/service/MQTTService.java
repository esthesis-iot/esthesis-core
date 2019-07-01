package esthesis.platform.server.service;

import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.mapper.MQTTServerMapper;
import esthesis.platform.server.model.MqttServer;
import esthesis.platform.server.repository.MQTTServerRepository;
import org.apache.commons.collections4.CollectionUtils;
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

  public MQTTService(
    MQTTServerMapper mqttServerMapper, MQTTServerRepository mqttServerRepository) {
    this.mqttServerMapper = mqttServerMapper;
    this.mqttServerRepository = mqttServerRepository;
  }

  public List<MQTTServerDTO> findActive() {
    return mqttServerMapper.map(mqttServerRepository.findAllByState(true));
  }

  @Override
  public MQTTServerDTO save(MQTTServerDTO dto) {
    // Save the MQTT server.
    dto = super.save(dto);

    return dto;
  }

  @Override
  public MQTTServerDTO deleteById(long id) {
    final MQTTServerDTO mqttServerDTO = super.deleteById(id);

    return mqttServerDTO;
  }

  public Optional<MQTTServerDTO> matchByTag(List<Long> tags) {
    Optional<MQTTServerDTO> mqttServerDTO;

    if (CollectionUtils.isEmpty(tags)) {
      mqttServerDTO = findAll().stream()
        .filter(MQTTServerDTO::getState)
        .filter(o -> o.getTags().isEmpty())
        .findAny();
    } else {
      mqttServerDTO = findAll().stream()
        .filter(MQTTServerDTO::getState)
        .filter(o -> CollectionUtils.intersection(tags, o.getTags()).size() == tags.size())
        .findAny();
    }

    return mqttServerDTO;
  }
}
