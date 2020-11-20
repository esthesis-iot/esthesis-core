package esthesis.backend.mapper;

import esthesis.backend.dto.DeviceKeyDTO;
import esthesis.backend.model.DeviceKey;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DeviceKeyMapper extends BaseMapper<DeviceKeyDTO, DeviceKey> {

}
