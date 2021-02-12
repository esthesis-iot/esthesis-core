package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.common.device.commands.CommandRequestDTO;
import esthesis.platform.backend.server.model.CommandRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
@Mapper(componentModel = "spring", uses = DeviceMapper.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CommandRequestMapper extends BaseMapper<CommandRequestDTO, CommandRequest>{

  @Mapping(source = "device.hardwareId", target = "deviceHardwareId")
  public abstract CommandRequestDTO map(CommandRequest entity);
}
