package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.DashboardWidgetDTO;
import esthesis.platform.server.model.DashboardWidget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses =
  DashboardMapper.class)
public abstract class DashboardWidgetMapper extends
  BaseMapper<DashboardWidgetDTO, DashboardWidget> {

}
