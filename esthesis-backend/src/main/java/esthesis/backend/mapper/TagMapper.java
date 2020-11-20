package esthesis.backend.mapper;

import esthesis.backend.dto.TagDTO;
import esthesis.backend.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class TagMapper extends BaseMapper<TagDTO, Tag> {

}
