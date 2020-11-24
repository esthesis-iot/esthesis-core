package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.ApplicationDTO;
import esthesis.platform.backend.server.model.Application;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ApplicationMapper extends BaseMapper<ApplicationDTO, Application> {

}
