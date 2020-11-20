package esthesis.backend.mapper;

import esthesis.common.device.commands.CommandReplyDTO;
import esthesis.backend.model.CommandReply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = DeviceMapper.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CommandReplyMapper extends BaseMapper<CommandReplyDTO, CommandReply> {

  @Mapping(source="entity.commandRequest.id", target="commandRequestId")
  public abstract CommandReplyDTO map(CommandReply entity);
}
