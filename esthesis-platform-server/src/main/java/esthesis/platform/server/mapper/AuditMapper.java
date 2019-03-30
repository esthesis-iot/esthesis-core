package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.AuditDTO;
import esthesis.platform.server.model.Audit;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class AuditMapper extends BaseMapper<AuditDTO, Audit> {

}
