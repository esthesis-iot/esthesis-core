package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.backend.server.model.NiFiSink;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class NiFiSinkMapper extends BaseMapper<NiFiSinkDTO, NiFiSink> {

}
