package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.VirtualizationDTO;
import esthesis.platform.server.model.Virtualization;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = {TagMapper.class, CertificateMapper.class}, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class VirtualizationMapper extends BaseMapper<VirtualizationDTO, Virtualization> {

}
