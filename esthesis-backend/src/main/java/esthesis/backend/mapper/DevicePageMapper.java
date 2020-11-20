package esthesis.backend.mapper;

import esthesis.backend.dto.DevicePageDTO;
import esthesis.backend.model.DevicePage;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DevicePageMapper extends BaseMapper<DevicePageDTO, DevicePage>  {

}
