package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.NiFiDTO;
import esthesis.platform.server.model.NiFi;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class NiFiMapper extends BaseMapper<NiFiDTO, NiFi>  {

}
