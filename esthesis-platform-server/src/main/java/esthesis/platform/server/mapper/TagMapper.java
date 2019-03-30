package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.TagDTO;
import esthesis.platform.server.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class TagMapper extends BaseMapper<TagDTO, Tag> {

}
