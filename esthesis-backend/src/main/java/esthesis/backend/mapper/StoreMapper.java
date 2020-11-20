package esthesis.backend.mapper;

import esthesis.backend.dto.StoreDTO;
import esthesis.backend.model.Store;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = {CertificateMapper.class,
  CaMapper.class}, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class StoreMapper extends BaseMapper<StoreDTO, Store> {

}
