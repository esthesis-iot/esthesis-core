package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.NiFiDTO;
import esthesis.platform.server.model.NiFi;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class NiFiMapper extends BaseMapper<NiFiDTO, NiFi>  {

  @Mapping(ignore = true, target = "modifiedBy")
  @Mapping(ignore = true, target = "modifiedOn")
  @Mapping(ignore = true, target = "createdBy")
  @Mapping(ignore = true, target = "version")
  @Mapping(ignore = true, target = "lastChecked")
  @Mapping(ignore = true, target = "wfVersion")
  @Mapping(ignore = true, target = "synced")
  public abstract void map(NiFiDTO dto, @MappingTarget NiFi entity);

}
