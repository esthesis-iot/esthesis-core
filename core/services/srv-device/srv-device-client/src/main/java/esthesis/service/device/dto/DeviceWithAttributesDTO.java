package esthesis.service.device.dto;

import esthesis.common.AppConstants;
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
public class DeviceWithAttributesDTO implements Serializable {

  private String deviceId;
  private String hardwareId;
  private List<DeviceAttributeEntity> attributes;
  private AppConstants.Device.Status status;
}
