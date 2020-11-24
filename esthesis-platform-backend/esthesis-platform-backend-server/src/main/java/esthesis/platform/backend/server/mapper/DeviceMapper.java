package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.common.device.dto.DeviceDTO;
import esthesis.platform.backend.server.model.Device;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DeviceMapper extends BaseMapper<DeviceDTO, Device>  {

}
