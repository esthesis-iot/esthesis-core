package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.StoreDTO;
import esthesis.platform.backend.server.model.Store;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = {CertificateMapper.class,
  CaMapper.class}, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class StoreMapper extends BaseMapper<StoreDTO, Store> {

}
