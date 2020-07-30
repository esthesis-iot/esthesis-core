package esthesis.platform.server.service;

import esthesis.platform.server.config.AppSettings.Setting.DeviceRegistration;
import esthesis.platform.server.config.AppSettings.SettingValues.DeviceRegistration.TagsAlgorithm;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.mapper.MQTTServerMapper;
import esthesis.platform.server.model.MqttServer;
import esthesis.platform.server.model.Tag;
import esthesis.platform.server.repository.MQTTServerRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Validated
@Transactional
public class MQTTService extends BaseService<MQTTServerDTO, MqttServer> {

  private final MQTTServerMapper mqttServerMapper;
  private final MQTTServerRepository mqttServerRepository;
  private final SettingResolverService srs;
  private final TagService tagService;

  public MQTTService(
    MQTTServerMapper mqttServerMapper, MQTTServerRepository mqttServerRepository,
    SettingResolverService srs, TagService tagService) {
    this.mqttServerMapper = mqttServerMapper;
    this.mqttServerRepository = mqttServerRepository;
    this.srs = srs;
    this.tagService = tagService;
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
    return super.deleteById(id);
  }

  /**
   * Finds an MQTT server with the given tags.
   *
   * @param tags A comma-separated list of tag names.
   * @return Returns the MQTT server registered with all given tags matched.
   */
  public Optional<MQTTServerDTO> matchByTag(String tags) {
    Optional<MQTTServerDTO> mqttServerDTO = Optional.empty();
    final String[] tagsArray = StringUtils.isNotEmpty(tags) ? tags.split(",") : new String[]{};

    if (StringUtils.isEmpty(tags)) {
      // If no tags were provided, find an MQTT server without tags.
      mqttServerDTO = findAll().stream()
        .filter(MQTTServerDTO::getState)
        .filter(o -> o.getTags().isEmpty())
        .findAny();
    } else {
      // Convert tag names to tag Ids and find matching registered MQTT servers.
      List<Long> tagIds = StreamSupport.stream(
        tagService.findAllByNameIn(Arrays.asList(tagsArray)).spliterator(), true)
        .map(Tag::getId)
        .collect(Collectors.toList());

      // Check whether an MQTT server matched according to the tag matching algorithm.
      switch (srs.get(DeviceRegistration.TAGS_ALGORITHM)) {
        case TagsAlgorithm.ALL:
          mqttServerDTO = findAll().stream()
            .filter(MQTTServerDTO::getState)
            .filter(
              o -> CollectionUtils.intersection(tagIds, o.getTags()).size() == o.getTags().size() &&
                   CollectionUtils.intersection(tagIds, o.getTags()).size() == tagIds.size()
            )
            .findFirst();
          break;
        case TagsAlgorithm.ANY:
          mqttServerDTO = findAll().stream()
            .filter(MQTTServerDTO::getState)
            .filter(o -> CollectionUtils.intersection(tagIds, o.getTags()).size() > 0)
            .findFirst();
          break;
        default:
          break;
      }
    }

    return mqttServerDTO;
  }
}
