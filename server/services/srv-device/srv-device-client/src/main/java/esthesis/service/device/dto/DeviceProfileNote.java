package esthesis.service.device.dto;

import esthesis.common.dto.BaseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DeviceProfileNote extends BaseDTO {

  private String deviceId;
  private String label;
  private String fieldName;
  private String fieldValue;
}
