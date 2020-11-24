package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.TagDTO;
import esthesis.platform.backend.server.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class TagMapper extends BaseMapper<TagDTO, Tag> {

}
