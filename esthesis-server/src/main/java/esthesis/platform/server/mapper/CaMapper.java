package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.CaDTO;
import esthesis.platform.server.model.Ca;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CaMapper extends BaseMapper<CaDTO, Ca> {

}
