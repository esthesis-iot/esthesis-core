package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.ProvisioningDTO;
import esthesis.platform.backend.server.model.Provisioning;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class, imports = StringUtils.class,
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ProvisioningMapper extends BaseMapper<ProvisioningDTO, Provisioning> {

}
