package esthesis.platform.server.mapper;

import esthesis.common.device.commands.CommandReplyDTO;
import esthesis.platform.server.model.CommandReply;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = DeviceMapper.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CommandReplyMapper extends BaseMapper<CommandReplyDTO, CommandReply> {

}
