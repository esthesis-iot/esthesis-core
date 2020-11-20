package esthesis.backend.mapper;

import esthesis.backend.dto.CaDTO;
import esthesis.backend.model.Ca;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CaMapper extends BaseMapper<CaDTO, Ca> {

}
