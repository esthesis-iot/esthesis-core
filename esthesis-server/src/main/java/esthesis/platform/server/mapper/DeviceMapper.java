package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.device.DeviceDTO;
import esthesis.platform.server.model.Device;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class,
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DeviceMapper extends BaseMapper<DeviceDTO, Device> {

}
