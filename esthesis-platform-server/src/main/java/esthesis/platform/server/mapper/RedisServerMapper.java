package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.RedisServerDTO;
import esthesis.platform.server.model.RedisServer;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class RedisServerMapper extends BaseMapper<RedisServerDTO, RedisServer> {

}
