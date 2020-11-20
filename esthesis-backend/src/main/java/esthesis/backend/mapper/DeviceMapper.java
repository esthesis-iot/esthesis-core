package esthesis.backend.mapper;

import esthesis.common.device.dto.DeviceDTO;
import esthesis.backend.model.Device;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DeviceMapper extends BaseMapper<DeviceDTO, Device>  {

}
