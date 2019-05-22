package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.ProvisioningDTO;
import esthesis.platform.server.model.Provisioning;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class, imports = StringUtils.class,
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ProvisioningMapper extends BaseMapper<ProvisioningDTO, Provisioning> {

  @Override
  @Mapping(target = "signed",
    expression = "java(StringUtils.isNotBlank(entity.getSignaturePlain()) && StringUtils"
      + ".isNotBlank(entity.getSignatureEncrypted()))")
  public abstract ProvisioningDTO map(Provisioning entity);

}
