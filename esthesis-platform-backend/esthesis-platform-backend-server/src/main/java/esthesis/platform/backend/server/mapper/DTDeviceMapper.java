package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.DTDeviceDTO;
import esthesis.platform.backend.server.model.Device;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DTDeviceMapper extends BaseMapper<DTDeviceDTO, Device>  {

}
