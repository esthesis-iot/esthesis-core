package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.DevicePageDTO;
import esthesis.platform.backend.server.model.DevicePage;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DevicePageMapper extends BaseMapper<DevicePageDTO, DevicePage>  {

}
