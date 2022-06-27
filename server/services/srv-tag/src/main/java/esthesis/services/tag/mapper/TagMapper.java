package esthesis.services.tag.mapper;

import esthesis.common.dto.Tag;
import esthesis.common.service.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "cdi", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class TagMapper extends BaseMapper<Tag> {

}
