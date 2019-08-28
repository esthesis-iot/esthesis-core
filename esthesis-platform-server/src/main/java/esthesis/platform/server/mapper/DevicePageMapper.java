package esthesis.platform.server.mapper;

import esthesis.common.datasink.dto.FieldDTO;
import esthesis.platform.server.model.DevicePage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class DevicePageMapper {

  public abstract FieldDTO map(DevicePage entity);

  public List<FieldDTO> map(Iterable<DevicePage> all) {
    return StreamSupport.stream(all.spliterator(), false).map(this::map)
      .collect(Collectors.toList());
  }

  public abstract DevicePage map(FieldDTO source, @MappingTarget DevicePage target);
}
