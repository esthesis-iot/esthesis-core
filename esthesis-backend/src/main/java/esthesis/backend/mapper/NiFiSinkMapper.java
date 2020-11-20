package esthesis.backend.mapper;

import esthesis.backend.dto.nifisinks.NiFiSinkDTO;
import esthesis.backend.model.NiFiSink;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class NiFiSinkMapper extends BaseMapper<NiFiSinkDTO, NiFiSink> {

}
