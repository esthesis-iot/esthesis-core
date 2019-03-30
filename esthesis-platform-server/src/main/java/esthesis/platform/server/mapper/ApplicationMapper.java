package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.model.Application;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ApplicationMapper extends BaseMapper<ApplicationDTO, Application> {

}
