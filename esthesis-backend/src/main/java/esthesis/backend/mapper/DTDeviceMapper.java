package esthesis.backend.mapper;

import esthesis.backend.dto.DTDeviceDTO;
import esthesis.backend.model.Device;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DTDeviceMapper extends BaseMapper<DTDeviceDTO, Device>  {

}
