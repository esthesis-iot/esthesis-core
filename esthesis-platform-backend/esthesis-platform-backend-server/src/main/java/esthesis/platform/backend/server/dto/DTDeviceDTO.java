package esthesis.platform.backend.server.dto;

import esthesis.platform.backend.common.device.dto.DeviceDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DTDeviceDTO extends DeviceDTO {
}
