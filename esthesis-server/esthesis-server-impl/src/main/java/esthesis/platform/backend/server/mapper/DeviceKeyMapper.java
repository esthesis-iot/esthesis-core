package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.DeviceKeyDTO;
import esthesis.platform.backend.server.model.DeviceKey;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DeviceKeyMapper extends BaseMapper<DeviceKeyDTO, DeviceKey> {

}
