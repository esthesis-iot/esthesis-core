package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.StoreDTO;
import esthesis.platform.server.model.Store;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = {CertificateMapper.class, CaMapper.class}, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class StoreMapper extends BaseMapper<StoreDTO, Store>  {

}
