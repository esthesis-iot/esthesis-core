package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.TunnelServerDTO;
import esthesis.platform.server.model.TunnelServer;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class TunnelServerMapper extends BaseMapper<TunnelServerDTO, TunnelServer> {

}
