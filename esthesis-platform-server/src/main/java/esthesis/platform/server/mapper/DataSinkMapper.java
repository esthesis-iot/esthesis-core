package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.DataSinkDTO;
import esthesis.platform.server.model.DataSink;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DataSinkMapper extends BaseMapper<DataSinkDTO, DataSink> {

}
