package esthesis.services.tag.mapper;

import esthesis.dto.Tag;
import esthesis.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "cdi", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class TagMapper extends BaseMapper<Tag> {

}
