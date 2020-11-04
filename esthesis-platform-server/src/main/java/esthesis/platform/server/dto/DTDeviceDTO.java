package esthesis.platform.server.dto;

import esthesis.common.device.dto.DeviceDTO;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DTDeviceDTO extends DeviceDTO {
}
