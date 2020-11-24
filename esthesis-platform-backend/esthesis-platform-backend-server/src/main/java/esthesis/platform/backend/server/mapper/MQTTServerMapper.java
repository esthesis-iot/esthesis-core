package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.MQTTServerDTO;
import esthesis.platform.backend.server.model.MqttServer;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class,
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class MQTTServerMapper extends BaseMapper<MQTTServerDTO, MqttServer> {

}
