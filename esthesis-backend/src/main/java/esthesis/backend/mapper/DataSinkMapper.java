package esthesis.backend.mapper;

import esthesis.backend.dto.DataSinkDTO;
import esthesis.backend.model.DataSink;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DataSinkMapper extends BaseMapper<DataSinkDTO, DataSink> {

}
