package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.DashboardDTO;
import esthesis.platform.backend.server.model.Dashboard;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DashboardMapper extends BaseMapper<DashboardDTO, Dashboard> {

}
