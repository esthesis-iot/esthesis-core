package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.CommandReplyDTO;
import esthesis.platform.server.model.CommandReply;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = CommandRequestMapper.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CommandReplyMapper extends BaseMapper<CommandReplyDTO, CommandReply>  {

}
