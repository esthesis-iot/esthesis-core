package esthesis.platform.server.mapper;

import esthesis.extension.device.response.ProvisioningInfoResponse;
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
    expression = "java(StringUtils.isNotBlank(entity.getSignature()))")
  @Mapping(target = "size", source = "fileSize")
  public abstract ProvisioningDTO map(Provisioning entity);

  @Mapping(target = "signed",
    expression = "java(StringUtils.isNotBlank(entity.getSignature()))")
  public abstract ProvisioningInfoResponse toProvisioningInfoResponse(Provisioning entity);
}
