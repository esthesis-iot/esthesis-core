package esthesis.platform.backend.server.dto;

import esthesis.platform.backend.common.device.dto.DeviceDTO;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceCoordinates extends DeviceDTO  {
  BigDecimal lon;
  BigDecimal lat;
  Instant coordinatesUpdatedOn;
}
