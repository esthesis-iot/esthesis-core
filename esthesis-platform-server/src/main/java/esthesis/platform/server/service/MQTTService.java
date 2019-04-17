package esthesis.platform.server.service;

import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONFIGURATION_MQTT;

import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.events.LocalEvent;
import esthesis.platform.server.mapper.MQTTServerMapper;
import esthesis.platform.server.model.MqttServer;
import esthesis.platform.server.repository.MQTTServerRepository;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
@Transactional
@Validated
public class MQTTService extends BaseService<MQTTServerDTO, MqttServer> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(MQTTService.class.getName());


  // Individual leader latches for all MQTT servers.
  private Map<Long, LeaderLatch> leaderLatchMQTT = new HashMap<>();

  // A list of active mqtt clients.
  private Map<Long, IMqttClient> mqttClients = new HashMap<>();

  private final MQTTServerMapper mqttServerMapper;
  private final MQTTServerRepository mqttServerRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  public MQTTService(MQTTServerMapper mqttServerMapper, MQTTServerRepository mqttServerRepository,
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
}
