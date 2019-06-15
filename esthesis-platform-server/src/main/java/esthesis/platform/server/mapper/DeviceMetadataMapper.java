package esthesis.platform.server.mapper;

import esthesis.extension.datasink.dto.MetadataFieldDTO;
import esthesis.platform.server.model.DeviceMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DeviceMetadataMapper {

  public abstract MetadataFieldDTO map(DeviceMetadata entity);

  public List<MetadataFieldDTO> map(Iterable<DeviceMetadata> all) {
    return StreamSupport.stream(all.spliterator(), false).map(this::map)
      .collect(Collectors.toList());
  }

  public abstract DeviceMetadata map(MetadataFieldDTO source, @MappingTarget DeviceMetadata target);
}
