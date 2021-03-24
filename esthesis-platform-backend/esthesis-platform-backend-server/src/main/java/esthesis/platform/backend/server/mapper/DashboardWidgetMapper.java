package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.DashboardWidgetDTO;
import esthesis.platform.backend.server.model.DashboardWidget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses =
  DashboardMapper.class)
public abstract class DashboardWidgetMapper extends
  BaseMapper<DashboardWidgetDTO, DashboardWidget> {

}
