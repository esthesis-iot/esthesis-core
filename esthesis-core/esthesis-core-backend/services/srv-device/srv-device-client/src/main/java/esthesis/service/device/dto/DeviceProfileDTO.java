package esthesis.service.device.dto;

import esthesis.service.device.entity.DeviceAttributeEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class DeviceProfileDTO implements Serializable {

  private List<DeviceAttributeEntity> attributes;
  private List<DeviceProfileFieldDataDTO> fields;
}
