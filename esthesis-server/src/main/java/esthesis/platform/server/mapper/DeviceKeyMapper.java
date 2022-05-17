package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.DeviceKeyDTO;
import esthesis.platform.server.model.DeviceKey;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DeviceKeyMapper extends BaseMapper<DeviceKeyDTO, DeviceKey> {

}
