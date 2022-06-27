package esthesis.common.service.mapper;

import esthesis.common.dto.BaseDTO;
import org.mapstruct.MappingTarget;

public abstract class BaseMapper<D extends BaseDTO> {

  public abstract D map(D sourceDTO, @MappingTarget D targetDTO);
}
