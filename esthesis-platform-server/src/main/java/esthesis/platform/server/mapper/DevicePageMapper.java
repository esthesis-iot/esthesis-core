package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.DevicePageDTO;
import esthesis.platform.server.model.DevicePage;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DevicePageMapper extends BaseMapper<DevicePageDTO, DevicePage>  {

}
