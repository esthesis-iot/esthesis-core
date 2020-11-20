package esthesis.backend.mapper;

import esthesis.backend.dto.ApplicationDTO;
import esthesis.backend.model.Application;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ApplicationMapper extends BaseMapper<ApplicationDTO, Application> {

}
