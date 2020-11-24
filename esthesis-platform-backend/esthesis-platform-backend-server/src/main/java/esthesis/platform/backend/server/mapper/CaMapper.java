package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.CaDTO;
import esthesis.platform.backend.server.model.Ca;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CaMapper extends BaseMapper<CaDTO, Ca> {

}
