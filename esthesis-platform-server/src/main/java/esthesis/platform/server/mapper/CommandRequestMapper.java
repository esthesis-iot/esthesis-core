package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.CommandRequestDTO;
import esthesis.platform.server.model.CommandRequest;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = DeviceMapper.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CommandRequestMapper extends BaseMapper<CommandRequestDTO, CommandRequest>  {

}
