package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.ProvisioningDTO;
import esthesis.platform.server.model.Provisioning;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class,
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ProvisioningMapper extends BaseMapper<ProvisioningDTO, Provisioning> {

  @Override
  @Mapping(ignore = true, target= "fileContent")
  public abstract ProvisioningDTO map(Provisioning entity);

}
