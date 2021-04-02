package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.DataSinkDTO;
import esthesis.platform.backend.server.model.DataSink;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DataSinkMapper extends BaseMapper<DataSinkDTO, DataSink> {

}
