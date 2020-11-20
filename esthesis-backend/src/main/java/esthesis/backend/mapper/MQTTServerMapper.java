package esthesis.backend.mapper;

import esthesis.backend.dto.MQTTServerDTO;
import esthesis.backend.model.MqttServer;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class,
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class MQTTServerMapper extends BaseMapper<MQTTServerDTO, MqttServer> {

}
