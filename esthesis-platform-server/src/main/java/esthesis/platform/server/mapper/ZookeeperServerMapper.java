package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.ZookeeperServerDTO;
import esthesis.platform.server.model.ZookeeperServer;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ZookeeperServerMapper extends BaseMapper<ZookeeperServerDTO, ZookeeperServer> {

}
